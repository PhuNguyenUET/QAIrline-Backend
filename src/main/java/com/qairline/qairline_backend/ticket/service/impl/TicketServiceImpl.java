package com.qairline.qairline_backend.ticket.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.qairline.qairline_backend.client.user.service.UserService;
import com.qairline.qairline_backend.common.exception.BusinessException;
import com.qairline.qairline_backend.common.payment.PaymentQueue;
import com.qairline.qairline_backend.flight.dto.AirportResponseDTO;
import com.qairline.qairline_backend.flight.dto.FlightResponseDTO;
import com.qairline.qairline_backend.flight.model.Airport;
import com.qairline.qairline_backend.flight.model.Flight;
import com.qairline.qairline_backend.flight.service.AirportService;
import com.qairline.qairline_backend.flight.service.CityService;
import com.qairline.qairline_backend.flight.service.FlightService;
import com.qairline.qairline_backend.flight.service.PlaneService;
import com.qairline.qairline_backend.mail.model.MailType;
import com.qairline.qairline_backend.mail.service.mail_template.MailTemplateService;
import com.qairline.qairline_backend.mail.service.send_mail.SendMailService;
import com.qairline.qairline_backend.ticket.dto.TicketAddRequestDTO;
import com.qairline.qairline_backend.ticket.dto.TicketEditRequestDTO;
import com.qairline.qairline_backend.ticket.dto.TicketResponseDTO;
import com.qairline.qairline_backend.ticket.model.SeatType;
import com.qairline.qairline_backend.ticket.model.Status;
import com.qairline.qairline_backend.ticket.model.Ticket;
import com.qairline.qairline_backend.ticket.model.TicketShoppingCart;
import com.qairline.qairline_backend.ticket.service.TicketService;
import com.qairline.qairline_backend.util.RandomUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final MongoTemplate mongoTemplate;
    private final FlightService flightService;
    private final UserService userService;
    private final AirportService airportService;
    private final PlaneService planeService;
    private final CityService cityService;

    private final MailTemplateService mailTemplateService;
    private final SendMailService sendMailService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private ModelMapper flightModelMapper;
    private ModelMapper airportModelMapper;
    private ModelMapper ticketModelMapper;

    private final Cache<String, Ticket> ticketCache =
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
        ticketModelMapper = new ModelMapper();

        ticketModelMapper.createTypeMap(Ticket.class, TicketResponseDTO.class)
                .setConverter(context -> {
                    Ticket ticket = context.getSource();
                    return TicketResponseDTO.builder()
                            .id(ticket.getId())
                            .name(ticket.getName())
                            .birthday(ticket.getBirthDay())
                            .address(ticket.getAddress())
                            .phone(ticket.getPhone())
                            .flight(flightModelMapper.map(flightService.findById(ticket.getFlight()), FlightResponseDTO.class))
                            .email(ticket.getEmail())
                            .passengerId(ticket.getPassengerId())
                            .seat(ticket.getSeat())
                            .seatType(ticket.getSeatType())
                            .price(ticket.getPrice())
                            .allowChangeUntil(ticket.getAllowedChangeUntil())
                            .allowPaymentUntil(ticket.getAllowedPaymentUntil())
                            .build();
                });
    }


    @Override
    public List<TicketResponseDTO> getAllTickets() {
        String userId = userService.getCurrentUser().getId();

        Query query = new Query();

        query.addCriteria(Criteria.where("customer").is(userId));

        return mongoTemplate.find(query, Ticket.class)
                .stream()
                .map(ticket -> ticketModelMapper.map(ticket, TicketResponseDTO.class))
                .toList();
    }

    @Override
    public TicketResponseDTO getTicketById(String id) {
        if(ticketCache.getIfPresent(id) != null) {
            return ticketModelMapper.map(ticketCache.getIfPresent(id), TicketResponseDTO.class);
        }

        Ticket ticket = mongoTemplate.findById(id, Ticket.class);

        if(ticket == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Ticket doesn't exist");
        } else {
            ticketCache.put(id, ticket);
            return ticketModelMapper.map(ticket, TicketResponseDTO.class);
        }

    }

    @Override
    public TicketResponseDTO bookTicket(TicketAddRequestDTO dto) {
        String userId = userService.getCurrentUser().getId();

        Flight flight = flightService.findById(dto.getFlight());

        if(flight == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Flight doesn't exist");
        }

        Calendar allowedEdit = Calendar.getInstance();
        allowedEdit.setTime(flight.getDepartureTime());
        allowedEdit.add(Calendar.DATE, -5);

        Calendar allowedPayment = Calendar.getInstance();
        allowedPayment.add(Calendar.HOUR, 1);

        if(dto.getTicketShoppingCart() != null) {
            TicketShoppingCart shoppingCart = mongoTemplate.findById(dto.getTicketShoppingCart(), TicketShoppingCart.class);
            if(shoppingCart != null) {
                mongoTemplate.remove(shoppingCart);
            }
        }

        Ticket ticket = Ticket.builder()
                .customer(userId)
                .flight(dto.getFlight())
                .name(dto.getName())
                .birthDay(dto.getBirthDay())
                .address(dto.getAddress())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .idCard(bCryptPasswordEncoder.encode(dto.getIdCard()))
                .seat(dto.getSeat())
                .seatType(dto.getSeatType().equals(SeatType.BUSINESS.name()) ? SeatType.BUSINESS.name() : SeatType.ECONOMY.name())
                .price(dto.getSeatType().equals(SeatType.BUSINESS.name()) ? flight.getBusinessPrice() : flight.getEconomyPrice())
                .passengerId(RandomUtils.generateRandomString(14))
                .status(Status.UNPAID.name())
                .bookingTime(new Date())
                .allowedChangeUntil(allowedEdit.getTime())
                .allowedPaymentUntil(allowedPayment.getTime())
                .build();

        flightService.bookFlight(dto.getFlight(), dto.getSeatType());
        ticket = mongoTemplate.save(ticket);

        PaymentQueue.getInstance().addToPaymentQueue(userId, ticket, false, 0);

        return ticketModelMapper.map(ticket, TicketResponseDTO.class);
    }

    @Override
    public TicketResponseDTO editTicket(TicketEditRequestDTO dto) {
        String userId = userService.getCurrentUser().getId();

        Ticket ticket = mongoTemplate.findById(dto.getId(), Ticket.class);

        if(ticket == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Ticket doesn't exist");
        }

        if(ticket.getAllowedChangeUntil().before(new Date())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Ticket cannot be edited anymore");
        }

        if(!ticket.getStatus().equals(Status.PAID.name())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Cannot edit unpaid or cancelled ticket");
        }

        String oldTicketSeatType = ticket.getSeatType();

        Flight flight = flightService.findById(ticket.getFlight());

        if(flight == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Flight doesn't exist");
        }

        ticket.setName(dto.getName());
        ticket.setBirthDay(dto.getBirthDay());
        ticket.setAddress(dto.getAddress());
        ticket.setPhone(dto.getPhone());
        ticket.setSeat(dto.getSeat());
        ticket.setEmail(dto.getEmail());
        ticket.setSeatType(dto.getSeatType().equals(SeatType.BUSINESS.name()) ? SeatType.BUSINESS.name() : SeatType.ECONOMY.name());
        ticket.setPrice(dto.getSeatType().equals(SeatType.BUSINESS.name()) ? flight.getBusinessPrice() : flight.getEconomyPrice());

        if(dto.getSeatType().equals(SeatType.BUSINESS.name()) && oldTicketSeatType.equals(SeatType.ECONOMY.name())) {
            Calendar allowedPayment = Calendar.getInstance();
            allowedPayment.add(Calendar.HOUR, 1);

            ticket.setStatus(Status.UNPAID.name());
            ticket.setAllowedPaymentUntil(allowedPayment.getTime());
            PaymentQueue.getInstance().addToPaymentQueue(userId, ticket, true, ticket.getPrice());
        } else if(dto.getSeatType().equals(SeatType.ECONOMY.name()) && oldTicketSeatType.equals(SeatType.BUSINESS.name())) {
            if(ticket.getStatus().equals(Status.PAID.name())) {
                refund(flight.getBusinessPrice() - flight.getEconomyPrice(), ticket.getEmail());
            }
        }

        if(dto.getIdCard() != null && !dto.getIdCard().isEmpty()) {
            ticket.setIdCard(bCryptPasswordEncoder.encode(dto.getIdCard()));
        }

        ticketCache.put(ticket.getId(), ticket);
        mongoTemplate.save(ticket);

        return ticketModelMapper.map(ticket, TicketResponseDTO.class);
    }

    @Override
    public List<TicketResponseDTO> bookTicketBatch(List<TicketAddRequestDTO> dtoList) {
        List<TicketResponseDTO> tickets = new ArrayList<>();
        for(TicketAddRequestDTO dto : dtoList) {
            tickets.add(bookTicket(dto));
        }
        return tickets;
    }

    @Override
    public List<TicketResponseDTO> filterTicket(Map<String, Object> filterMap) {
        List<Ticket> tickets;
        List<Flight> flights;

        Query airportQuery = new Query();
        Query flightQuery = new Query();

        if(filterMap.get("departureCity") != null) {
            flightQuery.addCriteria(Criteria.where("originAirport.city").is(filterMap.get("departureCity")));
        }

        if(filterMap.get("destinationCity") != null) {
            flightQuery.addCriteria(Criteria.where("destinationAirport.city").is(filterMap.get("destinationCity")));
        }

        if(filterMap.get("departureAirport") != null) {
            flightQuery.addCriteria(Criteria.where("originAirport").is(filterMap.get("departureAirport")));
        }

        if(filterMap.get("destinationAirport") != null) {
            flightQuery.addCriteria(Criteria.where("destinationAirport").is(filterMap.get("destinationAirport")));
        }

        filterMap.computeIfAbsent("departureTimeStart", k -> new Date(0));
        filterMap.computeIfAbsent("departureTimeEnd", k -> new Date(Long.MAX_VALUE));

        flightQuery.addCriteria(Criteria.where("departureTime").gte(filterMap.get("departureTimeStart")).lte(filterMap.get("departureTimeEnd")));

        flights = mongoTemplate.find(flightQuery, Flight.class);

        filterMap.computeIfAbsent("bookingTimeStart", k -> new  Date(0));
        filterMap.computeIfAbsent("bookingTimeEnd", k -> new Date(Long.MAX_VALUE));

        airportQuery.addCriteria(Criteria.where("bookingTime").gte(filterMap.get("bookingTimeStart")).lte(filterMap.get("bookingTimeEnd")));
        if(!flights.isEmpty()) {
            airportQuery.addCriteria(Criteria.where("flight").in(flights.stream().map(Flight::getId).toList()));
        }

        tickets = mongoTemplate.find(airportQuery, Ticket.class);
        return tickets.stream().map(ticket -> ticketModelMapper.map(ticket, TicketResponseDTO.class)).toList();
    }

    static final String REFUND_SUBJECT = "QAirline - Refund";

    private void sendRefundEmail(String email, double amount) {
        String template = mailTemplateService.getTemplate(MailType.REFUND);
        String content = template.replace("{REFUND_AMOUNT}", String.valueOf(amount));
        sendMailService.addToQueue(email, REFUND_SUBJECT, content);
    }

    @Override
    public void cancelTicket(String id) {
        Ticket ticket = mongoTemplate.findById(id, Ticket.class);

        if(ticket == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Ticket doesn't exist");
        }

        ticket.setStatus(Status.CANCELLED.name());
        mongoTemplate.save(ticket);
    }

    @Override
    public void deleteTicket(String id) {
        Ticket ticket = mongoTemplate.findById(id, Ticket.class);

        if(ticket == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Ticket doesn't exist");
        }

        if(ticket.getAllowedChangeUntil().before(new Date())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Ticket cannot be edited anymore");
        }

        if(ticket.getStatus().equals(Status.PAID.name())) {
            refund(ticket.getPrice(), ticket.getEmail());
        }

        flightService.removeBookFlight(ticket.getFlight(), ticket.getSeatType());

        ticketCache.invalidate(id);
        mongoTemplate.remove(ticket);
    }

    @Override
    public void makePayment(String id) {
        Ticket ticket = mongoTemplate.findById(id, Ticket.class);

        if(ticket == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Ticket doesn't exist");
        }

        if(ticket.getAllowedPaymentUntil().before(new Date())) {
            ticket.setStatus(Status.CANCELLED.name());
            mongoTemplate.save(ticket);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Ticket cannot be paid for anymore");
        }

        if(!ticket.getStatus().equals(Status.UNPAID.name())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Ticket has been cancelled or has already been paid");
        }

        ticket.setStatus(Status.PAID.name());
        mongoTemplate.save(ticket);
    }

    @Override
    public void makePaymentBatch(List<String> tickets) {
        for(String ticketId : tickets) {
            makePayment(ticketId);
        }
    }

    @Override
    public void refund(double amount, String email) {
        sendRefundEmail(email, amount);
    }
}
