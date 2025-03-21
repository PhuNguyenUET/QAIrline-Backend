package com.qairline.qairline_backend.flight.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.qairline.qairline_backend.client.model.Role;
import com.qairline.qairline_backend.common.exception.BusinessException;
import com.qairline.qairline_backend.flight.dto.*;
import com.qairline.qairline_backend.flight.model.Airport;
import com.qairline.qairline_backend.flight.model.Flight;
import com.qairline.qairline_backend.flight.model.Plane;
import com.qairline.qairline_backend.flight.service.AirportService;
import com.qairline.qairline_backend.flight.service.CityService;
import com.qairline.qairline_backend.flight.service.FlightService;
import com.qairline.qairline_backend.flight.service.PlaneService;
import com.qairline.qairline_backend.mail.model.MailType;
import com.qairline.qairline_backend.mail.service.mail_template.MailTemplateService;
import com.qairline.qairline_backend.mail.service.send_mail.SendMailService;
import com.qairline.qairline_backend.ticket.model.SeatType;
import com.qairline.qairline_backend.ticket.model.Status;
import com.qairline.qairline_backend.ticket.model.Ticket;
import com.qairline.qairline_backend.util.RandomUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {
    private final MongoTemplate mongoTemplate;
    private final AirportService airportService;
    private final PlaneService planeService;
    private final CityService cityService;
    private ModelMapper flightModelMapper;
    private ModelMapper airportModelMapper;

    private final MailTemplateService mailTemplateService;
    private final SendMailService sendMailService;

    private final Cache<String, Flight> flightCache =
            CacheBuilder.newBuilder()
                    .maximumSize(500)
                    .expireAfterAccess(45, TimeUnit.MINUTES)
                    .build();

    @PostConstruct
    private void setUpModelMapper() {
        airportModelMapper = new ModelMapper();

        airportModelMapper.createTypeMap(Airport.class, AirportResponseDTO.class)
                .setConverter(context -> {
                    Airport airport = context.getSource();
                    return AirportResponseDTO.builder()
                            .id(airport.getId())
                            .airportCode(airport.getAirportCode())
                            .airportName(airport.getAirportName())
                            .city(cityService.findById(airport.getCityId()))
                            .location(airport.getLocation())
                            .build();
                });

        flightModelMapper = new ModelMapper();

        flightModelMapper.createTypeMap(Flight.class, FlightResponseDTO.class)
                .setConverter(context -> {
                    Flight flight = context.getSource();
                    return FlightResponseDTO.builder()
                            .id(flight.getId())
                            .departureTime(flight.getDepartureTime())
                            .arrivalTime(flight.getArrivalTime())
                            .originAirport(airportModelMapper.map(airportService.findById(flight.getOriginAirport()), AirportResponseDTO.class))
                            .destinationAirport(airportModelMapper.map(airportService.findById(flight.getDestinationAirport()), AirportResponseDTO.class))
                            .availableBusinessSeats(flight.getAvailableBusinessSeats())
                            .delayed(flight.isDelayed())
                            .flightNumber(flight.getFlightNumber())
                            .delayedDepartureTime(flight.getDelayedDepartureTime())
                            .availableEconomySeats(flight.getAvailableEconomySeats())
                            .economyPrice(flight.getEconomyPrice())
                            .businessPrice(flight.getBusinessPrice())
                            .discountPercent(flight.getDiscountPercent())
                            .isAvailable(flight.isAvailable())
                            .plane(planeService.findById(flight.getPlane()))
                            .build();
                });
    }

    @Override
    public List<FlightResponseDTO> getAllFlights() {
        return mongoTemplate.findAll(Flight.class)
                .stream()
                .map(flight -> flightModelMapper.map(flight, FlightResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<FlightResponseDTO> filterFlights(Map<String, Object> filterMap) {
        List<Flight> flights;

        Query flightQuery = new Query();
        if(filterMap.get("departureCity") != null) {
            List<String> departureAirport;
            Query airportQuery = new Query();
            airportQuery.addCriteria(Criteria.where("cityId").is(filterMap.get("departureCity")));
            departureAirport = mongoTemplate.find(airportQuery, Airport.class).stream().map(Airport::getId).toList();
            flightQuery.addCriteria(Criteria.where("originAirport").in(departureAirport));
        }

        if(filterMap.get("destinationCity") != null) {
            List<String> destinationAirport;
            Query airportQuery = new Query();
            airportQuery.addCriteria(Criteria.where("cityId").is(filterMap.get("destinationCity")));
            destinationAirport = mongoTemplate.find(airportQuery, Airport.class).stream().map(Airport::getId).toList();
            flightQuery.addCriteria(Criteria.where("destinationAirport").in(destinationAirport));
        }

        if(filterMap.get("departureAirport") != null) {
            flightQuery.addCriteria(Criteria.where("originAirport").is(filterMap.get("departureAirport")));
        }

        if(filterMap.get("destinationAirport") != null) {
            flightQuery.addCriteria(Criteria.where("destinationAirport").is(filterMap.get("destinationAirport")));
        }

        filterMap.computeIfAbsent("departureTimeStart", k -> new Date());

        filterMap.computeIfAbsent("departureTimeEnd", k -> new Date(Long.MAX_VALUE));

        filterMap.computeIfAbsent("destinationTimeStart", k -> new Date());

        filterMap.computeIfAbsent("destinationTimeEnd", k -> new Date(Long.MAX_VALUE));

        if(filterMap.get("seatType") != null && filterMap.get("priceRange") != null) {
            String[] parts = ((String) filterMap.get("priceRange")).split("-");

            int start = Integer.parseInt(parts[0].trim());
            int end = Integer.parseInt(parts[1].trim());

            if(filterMap.get("seatType").equals(SeatType.BUSINESS.name())) {
                flightQuery.addCriteria(Criteria.where("businessPrice").gte(start).lte(end));
            } else if (filterMap.get("seatType").equals(SeatType.ECONOMY.name())) {
                flightQuery.addCriteria(Criteria.where("economyPrice").gte(start).lte(end));
            }
        }

        flightQuery.addCriteria(Criteria.where("departureTime").gte(filterMap.get("departureTimeStart")).lte(filterMap.get("departureTimeEnd")));
        flightQuery.addCriteria(Criteria.where("arrivalTime").gte(filterMap.get("destinationTimeStart")).lte(filterMap.get("destinationTimeEnd")));

        if(filterMap.get("role").equals(Role.USER)) {
            flightQuery.addCriteria(Criteria.where("availableEconomySeats").gte(0));
            flightQuery.addCriteria(Criteria.where("availableBusinessSeats").gte(0));
            flightQuery.addCriteria(Criteria.where("isAvailable").is(true));
        }

        flights = mongoTemplate.find(flightQuery, Flight.class);

        return flights
                .stream()
                .map(flight -> flightModelMapper.map(flight, FlightResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Flight findById(String id) {
        if (flightCache.getIfPresent(id) != null) {
            return flightCache.getIfPresent(id);
        }

        Flight flight = mongoTemplate.findById(id, Flight.class);

        if(flight == null) {
            return null;
        } else {
            flightCache.put(id, flight);
            return flight;
        }
    }

    @Override
    public FlightResponseDTO getFlightById(String id) {
        return flightModelMapper.map(findById(id), FlightResponseDTO.class);
    }

    @Override
    public void bookFlight(String id, String seatType) {
        Flight flight = mongoTemplate.findById(id, Flight.class);

        if(flight == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Flight does not exist");
        }

        if(seatType.equals(SeatType.BUSINESS.name())) {
            flight.setAvailableBusinessSeats(flight.getAvailableBusinessSeats() - 1);
        } else if (seatType.equals(SeatType.ECONOMY.name())) {
            flight.setAvailableEconomySeats(flight.getAvailableEconomySeats() - 1);
        }
        if(flight.getAvailableBusinessSeats() <= 0 && flight.getAvailableEconomySeats() <= 0) {
            flight.setAvailable(false);
        }

        flightCache.put(flight.getId(), flight);
        mongoTemplate.save(flight);
    }

    @Override
    public void removeBookFlight(String id, String seatType) {
        Flight flight = mongoTemplate.findById(id, Flight.class);

        if(flight == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Flight does not exist");
        }

        if(seatType.equals(SeatType.BUSINESS.name())) {
            flight.setAvailableBusinessSeats(flight.getAvailableBusinessSeats() + 1);
        } else if (seatType.equals(SeatType.ECONOMY.name())) {
            flight.setAvailableEconomySeats(flight.getAvailableEconomySeats() + 1);
        }

        if(flight.getAvailableEconomySeats() > 0 || flight.getAvailableBusinessSeats() > 0) {
            flight.setAvailable(true);
        }

        flightCache.put(flight.getId(), flight);
        mongoTemplate.save(flight);
    }

    @Override
    public void addFlight(FlightRequestDTO dto) {
        Plane plane = planeService.findById(dto.getPlane());
        Airport destinationAirport = airportService.findById(dto.getDestinationAirport());
        Airport originAirport = airportService.findById(dto.getOriginAirport());

        if (plane == null || destinationAirport == null || originAirport == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Airport or plane does not exist");
        }

        String flightNumber = RandomUtils.generateRandomString(6);

        Query query = new Query();
        query.addCriteria(Criteria.where("flightNumber").is(flightNumber));

        while(mongoTemplate.exists(query, Flight.class)) {
            flightNumber = RandomUtils.generateRandomString(6);
            query = new Query();
            query.addCriteria(Criteria.where("flightNumber").is(flightNumber));
        }

        Flight flight = Flight.builder()
                .departureTime(dto.getDepartureTime())
                .flightNumber(RandomUtils.generateRandomString(6))
                .arrivalTime(dto.getArrivalTime())
                .originAirport(dto.getOriginAirport())
                .destinationAirport(dto.getDestinationAirport())
                .availableBusinessSeats(plane.getNumOfBusinessSeats())
                .availableEconomySeats(plane.getNumOfEconomySeats())
                .economyPrice(dto.getEconomyPrice())
                .flightNumber(flightNumber)
                .delayed(false)
                .delayedDepartureTime(dto.getDepartureTime())
                .businessPrice(dto.getBusinessPrice())
                .discountPercent(dto.getDiscountPercent())
                .isAvailable(dto.isAvailable())
                .plane(dto.getPlane())
                .build();

        mongoTemplate.save(flight);
    }

    @Override
    public void addFlightBatch(FlightRequestBatchDTO dto) {
        Plane plane = planeService.findById(dto.getPlane());
        Airport destinationAirport = airportService.findById(dto.getDestinationAirport());
        Airport originAirport = airportService.findById(dto.getOriginAirport());

        if (plane == null || destinationAirport == null || originAirport == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Airport or plane does not exist");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dto.getAddFrom());
        calendar.set(Calendar.DAY_OF_WEEK, dto.getDayOfWeekDeparture().getValue());
        if(calendar.getTime().before(new Date())) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        while(calendar.getTime().before(dto.getAddUntil())) {
            String flightNumber = RandomUtils.generateRandomString(6);

            Query query = new Query();
            query.addCriteria(Criteria.where("flightNumber").is(flightNumber));

            while(mongoTemplate.exists(query, Flight.class)) {
                flightNumber = RandomUtils.generateRandomString(6);
                query = new Query();
                query.addCriteria(Criteria.where("flightNumber").is(flightNumber));
            }

            Calendar departureTime = Calendar.getInstance();
            departureTime.setTime(calendar.getTime());
            departureTime.set(Calendar.HOUR_OF_DAY, dto.getDepartureTime().getHours());
            departureTime.set(Calendar.MINUTE, dto.getDepartureTime().getMinutes());
            departureTime.set(Calendar.SECOND, dto.getDepartureTime().getSeconds());

            Calendar arrivalTime = Calendar.getInstance();
            arrivalTime.setTime(calendar.getTime());
            arrivalTime.set(Calendar.HOUR_OF_DAY, dto.getArrivalTime().getHours());
            arrivalTime.set(Calendar.MINUTE, dto.getArrivalTime().getMinutes());
            arrivalTime.set(Calendar.SECOND, dto.getArrivalTime().getSeconds());

            if(dto.getArrivalTime().before(dto.getDepartureTime())) {
                arrivalTime.add(Calendar.DAY_OF_YEAR, 1);
            }

            Flight flight = Flight.builder()
                    .departureTime(departureTime.getTime())
                    .arrivalTime(arrivalTime.getTime())
                    .originAirport(dto.getOriginAirport())
                    .flightNumber(flightNumber)
                    .destinationAirport(dto.getDestinationAirport())
                    .availableBusinessSeats(plane.getNumOfBusinessSeats())
                    .availableEconomySeats(plane.getNumOfEconomySeats())
                    .economyPrice(dto.getEconomyPrice())
                    .delayed(false)
                    .delayedDepartureTime(departureTime.getTime())
                    .businessPrice(dto.getBusinessPrice())
                    .discountPercent(dto.getDiscountPercent())
                    .isAvailable(true)
                    .plane(dto.getPlane())
                    .build();

            mongoTemplate.save(flight);

            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }
    }

    @Override
    public void editFlight(FlightEditRequest dto) {
        Plane plane = planeService.findById(dto.getPlane());
        Airport destinationAirport = airportService.findById(dto.getDestinationAirport());
        Airport originAirport = airportService.findById(dto.getOriginAirport());

        if (plane == null || destinationAirport == null || originAirport == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Airport or plane does not exist");
        }

        Flight flight = mongoTemplate.findById(dto.getId(), Flight.class);

        if (flight == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Flight does not exist");
        }

        flight.setDepartureTime(dto.getDepartureTime());
        flight.setArrivalTime(dto.getArrivalTime());
        flight.setOriginAirport(dto.getOriginAirport());
        flight.setDestinationAirport(dto.getDestinationAirport());
        flight.setAvailableEconomySeats(flight.getAvailableEconomySeats());
        flight.setAvailableBusinessSeats(flight.getAvailableBusinessSeats());
        flight.setEconomyPrice(dto.getEconomyPrice());
        flight.setBusinessPrice(dto.getBusinessPrice());
        flight.setDiscountPercent(dto.getDiscountPercent());
        flight.setAvailable(dto.isAvailable());
        flight.setPlane(dto.getPlane());

        flightCache.put(flight.getId(), flight);
        mongoTemplate.save(flight);
    }

    @Override
    public void deleteFlight(String id) {
        Flight flight = mongoTemplate.findById(id, Flight.class);

        if(flight == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Flight does not exist");
        }

        flightCache.invalidate(flight.getId());
        mongoTemplate.remove(flight);

        if((new Date()).before(flight.getDepartureTime())) {
            Query query = new Query();
            query.addCriteria(Criteria.where("flight").is(id));

            List<Ticket> cancelledTicket = mongoTemplate.find(query, Ticket.class);

            for (Ticket ticket : cancelledTicket) {
                ticket.setStatus(Status.CANCELLED.name());
                sendRefundEmail(ticket.getEmail(), ticket.getPrice());
                mongoTemplate.save(ticket);
            }
        }
    }

    @Override
    public void openFlight(String id) {
        Flight flight = mongoTemplate.findById(id, Flight.class);

        if(flight == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Flight does not exist");
        }

        flight.setAvailable(true);

        flightCache.put(flight.getId(), flight);
        mongoTemplate.save(flight);
    }

    @Override
    public void closeFlight(String id) {
        Flight flight = mongoTemplate.findById(id, Flight.class);

        if(flight == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Flight does not exist");
        }

        flight.setAvailable(false);

        flightCache.put(flight.getId(), flight);
        mongoTemplate.save(flight);

        if((new Date()).before(flight.getDepartureTime())) {
            Query query = new Query();
            query.addCriteria(Criteria.where("flight").is(id));

            List<Ticket> cancelledTicket = mongoTemplate.find(query, Ticket.class);

            for (Ticket ticket : cancelledTicket) {
                ticket.setStatus(Status.CANCELLED.name());
                sendRefundEmail(ticket.getEmail(), ticket.getPrice());
                mongoTemplate.save(ticket);
            }
        }
    }

    static final String REFUND_SUBJECT = "QAirline - Refund";

    private void sendRefundEmail(String email, double amount) {
        String template = mailTemplateService.getTemplate(MailType.REFUND);
        String content = template.replace("{REFUND_AMOUNT}", String.valueOf(amount));
        sendMailService.addToQueue(email, REFUND_SUBJECT, content);
    }

    @Scheduled(cron = "0 0 0 ? * MON")
    public void deleteOldFlights() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -30);

        Date timeLimit = calendar.getTime();

        Query query = new Query();
        query.addCriteria(Criteria.where("arrivalTime").lte(timeLimit));

        List<Flight> oldFlights = mongoTemplate.find(query, Flight.class);

        oldFlights.forEach(mongoTemplate::remove);
    }

    @Override
    public void delayFlight(String id, Date newDepartureTime) {
        Flight flight = mongoTemplate.findById(id, Flight.class);

        if(flight == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Flight does not exist");
        }

        flight.setDelayed(true);
        flight.setDelayedDepartureTime(newDepartureTime);

        flightCache.put(flight.getId(), flight);
        mongoTemplate.save(flight);
    }
}
