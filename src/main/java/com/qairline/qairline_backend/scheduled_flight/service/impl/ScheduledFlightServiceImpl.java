package com.qairline.qairline_backend.scheduled_flight.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.qairline.qairline_backend.common.exception.BusinessException;
import com.qairline.qairline_backend.flight.dto.FlightRequestDTO;
import com.qairline.qairline_backend.flight.model.Airport;
import com.qairline.qairline_backend.flight.model.Flight;
import com.qairline.qairline_backend.flight.model.Plane;
import com.qairline.qairline_backend.flight.service.AirportService;
import com.qairline.qairline_backend.flight.service.FlightService;
import com.qairline.qairline_backend.flight.service.PlaneService;
import com.qairline.qairline_backend.scheduled_flight.dto.ScheduledFlightEditRequest;
import com.qairline.qairline_backend.scheduled_flight.dto.ScheduledFlightRequestDTO;
import com.qairline.qairline_backend.scheduled_flight.dto.ScheduledFlightResponseDTO;
import com.qairline.qairline_backend.scheduled_flight.model.DayOfWeek;
import com.qairline.qairline_backend.scheduled_flight.model.ScheduledFlight;
import com.qairline.qairline_backend.scheduled_flight.service.ScheduledFlightService;
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

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduledFlightServiceImpl implements ScheduledFlightService {
    private final MongoTemplate mongoTemplate;
    private final AirportService airportService;
    private final PlaneService planeService;
    private final FlightService flightService;
    private ModelMapper modelMapper;

    private final Cache<String, ScheduledFlight> scheduledFlightCache =
            CacheBuilder.newBuilder()
                    .maximumSize(500)
                    .expireAfterAccess(45, TimeUnit.MINUTES)
                    .build();

    @PostConstruct
    private void setUpModelMapper() {
        modelMapper = new ModelMapper();

        modelMapper.createTypeMap(ScheduledFlight.class, ScheduledFlightResponseDTO.class)
                .setConverter(context -> {
                    ScheduledFlight flight = context.getSource();
                    return ScheduledFlightResponseDTO.builder()
                            .id(flight.getId())
                            .departureTime(flight.getDepartureTime())
                            .arrivalTime(flight.getArrivalTime())
                            .validUntil(flight.getValidUntil())
                            .dayOfWeekDeparture(flight.getDayOfWeekDeparture())
                            .originAirport(airportService.findById(flight.getOriginAirport()))
                            .destinationAirport(airportService.findById(flight.getDestinationAirport()))
                            .availableBusinessSeats(flight.getAvailableBusinessSeats())
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
    public List<ScheduledFlightResponseDTO> getAllScheduledFlights() {
        return mongoTemplate.findAll(ScheduledFlight.class)
                .stream()
                .map(flight -> modelMapper.map(flight, ScheduledFlightResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ScheduledFlight findById(String id) {
        if (scheduledFlightCache.getIfPresent(id) != null) {
            return scheduledFlightCache.getIfPresent(id);
        }

        ScheduledFlight flight = mongoTemplate.findById(id, ScheduledFlight.class);

        if(flight == null) {
            return null;
        } else {
            scheduledFlightCache.put(id, flight);
            return flight;
        }
    }

    @Override
    public ScheduledFlightResponseDTO getScheduledFlightById(String id) {
        if (scheduledFlightCache.getIfPresent(id) != null) {
            return modelMapper.map(scheduledFlightCache.getIfPresent(id), ScheduledFlightResponseDTO.class);
        }

        ScheduledFlight flight = mongoTemplate.findById(id, ScheduledFlight.class);

        if(flight == null) {
            return null;
        } else {
            scheduledFlightCache.put(id, flight);
            return modelMapper.map(flight, ScheduledFlightResponseDTO.class);
        }
    }

    @Override
    public void addScheduledFlight(ScheduledFlightRequestDTO dto) {
        Plane plane = planeService.findById(dto.getPlane());
        Airport destinationAirport = airportService.findById(dto.getDestinationAirport());
        Airport originAirport = airportService.findById(dto.getOriginAirport());

        if (plane == null || destinationAirport == null || originAirport == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Airport or plane does not exist");
        }

        ScheduledFlight flight = ScheduledFlight.builder()
                .departureTime(dto.getDepartureTime())
                .arrivalTime(dto.getArrivalTime())
                .originAirport(dto.getOriginAirport())
                .destinationAirport(dto.getDestinationAirport())
                .availableBusinessSeats(plane.getNumOfBusinessSeats())
                .availableEconomySeats(plane.getNumOfEconomySeats())
                .economyPrice(dto.getEconomyPrice())
                .businessPrice(dto.getBusinessPrice())
                .discountPercent(dto.getDiscountPercent())
                .isAvailable(dto.isAvailable())
                .plane(dto.getPlane())
                .dayOfWeekDeparture(dto.getDayOfWeekDeparture())
                .validUntil(dto.getValidUntil())
                .build();

        mongoTemplate.save(flight);
    }

    @Override
    public void editScheduledFlight(ScheduledFlightEditRequest dto) {
        Plane plane = planeService.findById(dto.getPlane());
        Airport destinationAirport = airportService.findById(dto.getDestinationAirport());
        Airport originAirport = airportService.findById(dto.getOriginAirport());

        if (plane == null || destinationAirport == null || originAirport == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Airport or plane does not exist");
        }

        ScheduledFlight flight = mongoTemplate.findById(dto.getId(), ScheduledFlight.class);

        if (flight == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Scheduled flight does not exist");
        }

        flight.setDepartureTime(dto.getDepartureTime());
        flight.setArrivalTime(dto.getArrivalTime());
        flight.setOriginAirport(dto.getOriginAirport());
        flight.setDestinationAirport(dto.getDestinationAirport());
        flight.setAvailableEconomySeats(flight.getAvailableEconomySeats());
        flight.setDayOfWeekDeparture(dto.getDayOfWeekDeparture());
        flight.setValidUntil(dto.getValidUntil());
        flight.setAvailableBusinessSeats(flight.getAvailableBusinessSeats());
        flight.setEconomyPrice(dto.getEconomyPrice());
        flight.setBusinessPrice(dto.getBusinessPrice());
        flight.setDiscountPercent(dto.getDiscountPercent());
        flight.setAvailable(dto.isAvailable());
        flight.setPlane(dto.getPlane());

        scheduledFlightCache.put(flight.getId(), flight);
        mongoTemplate.save(flight);
    }

    @Override
    public void deleteScheduledFlight(String id) {
        ScheduledFlight flight = mongoTemplate.findById(id, ScheduledFlight.class);

        if(flight == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Scheduled flight does not exist");
        }

        scheduledFlightCache.invalidate(flight.getId());
        mongoTemplate.remove(flight);
    }

    @Override
    public void openScheduledFlight(String id) {
        ScheduledFlight flight = mongoTemplate.findById(id, ScheduledFlight.class);

        if(flight == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Scheduled flight does not exist");
        }

        flight.setAvailable(true);

        scheduledFlightCache.put(flight.getId(), flight);
        mongoTemplate.save(flight);
    }

    @Override
    public void closeScheduledFlight(String id) {
        ScheduledFlight flight = mongoTemplate.findById(id, ScheduledFlight.class);

        if(flight == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Scheduled flight does not exist");
        }

        flight.setAvailable(false);

        scheduledFlightCache.put(flight.getId(), flight);
        mongoTemplate.save(flight);
    }

    private void createFlightFromScheduledFlight(ScheduledFlight scheduledFlight) {
        String flightNumber = RandomUtils.generateRandomString(6);

        Query query = new Query();
        query.addCriteria(Criteria.where("flightNumber").is(flightNumber));

        while(mongoTemplate.exists(query, Flight.class)) {
            flightNumber = RandomUtils.generateRandomString(6);
            query = new Query();
            query.addCriteria(Criteria.where("flightNumber").is(flightNumber));
        }

        Calendar departureCalendar = Calendar.getInstance();
        departureCalendar.add(Calendar.WEEK_OF_YEAR, 2);
        departureCalendar.set(Calendar.HOUR_OF_DAY, scheduledFlight.getDepartureTime().getHours());
        departureCalendar.set(Calendar.MINUTE, scheduledFlight.getDepartureTime().getMinutes());
        departureCalendar.set(Calendar.SECOND, scheduledFlight.getDepartureTime().getSeconds());

        Calendar arrivalCalendar = Calendar.getInstance();
        arrivalCalendar.add(Calendar.WEEK_OF_YEAR, 2);
        if(scheduledFlight.getArrivalTime().before(scheduledFlight.getDepartureTime())) {
            arrivalCalendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        arrivalCalendar.set(Calendar.HOUR_OF_DAY, scheduledFlight.getArrivalTime().getHours());
        arrivalCalendar.set(Calendar.MINUTE, scheduledFlight.getArrivalTime().getMinutes());
        arrivalCalendar.set(Calendar.SECOND, scheduledFlight.getArrivalTime().getSeconds());

        Flight flight = Flight.builder()
                .flightNumber(flightNumber)
                .departureTime(departureCalendar.getTime())
                .arrivalTime(arrivalCalendar.getTime())
                .originAirport(scheduledFlight.getOriginAirport())
                .destinationAirport(scheduledFlight.getDestinationAirport())
                .availableBusinessSeats(scheduledFlight.getAvailableBusinessSeats())
                .availableEconomySeats(scheduledFlight.getAvailableEconomySeats())
                .economyPrice(scheduledFlight.getEconomyPrice())
                .businessPrice(scheduledFlight.getBusinessPrice())
                .discountPercent(scheduledFlight.getDiscountPercent())
                .isAvailable(scheduledFlight.isAvailable())
                .plane(scheduledFlight.getPlane())
                .build();

        mongoTemplate.save(flight);
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void createFlightFromScheduledFlight() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, 2);

        Query deleteQuery = new Query();
        deleteQuery.addCriteria(Criteria.where("validUntil").lt(calendar.getTime()));
        List<ScheduledFlight> scheduledFlightsToDelete = mongoTemplate.find(deleteQuery, ScheduledFlight.class);

        for (ScheduledFlight flight : scheduledFlightsToDelete) {
            mongoTemplate.remove(flight);
        }

        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        DayOfWeek dayOfWeek = DayOfWeek.fromInt(currentDayOfWeek);

        Query query = new Query();
        query.addCriteria(Criteria.where("isAvailable").is(true));
        query.addCriteria(Criteria.where("dayOfWeekDeparture").is(dayOfWeek));
        List<ScheduledFlight> scheduledFlights = mongoTemplate.find(query, ScheduledFlight.class);

        for (ScheduledFlight flight : scheduledFlights) {
            createFlightFromScheduledFlight(flight);
        }
    }

}
