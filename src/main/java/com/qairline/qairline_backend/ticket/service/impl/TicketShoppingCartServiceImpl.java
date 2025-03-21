package com.qairline.qairline_backend.ticket.service.impl;

import com.qairline.qairline_backend.client.user.model.User;
import com.qairline.qairline_backend.common.exception.BusinessException;
import com.qairline.qairline_backend.flight.model.Flight;
import com.qairline.qairline_backend.ticket.dto.TicketCartAddDTO;
import com.qairline.qairline_backend.ticket.dto.TicketCartEditDTO;
import com.qairline.qairline_backend.ticket.model.SeatType;
import com.qairline.qairline_backend.ticket.model.TicketShoppingCart;
import com.qairline.qairline_backend.ticket.repository.TicketShoppingCartRepository;
import com.qairline.qairline_backend.ticket.service.TicketShoppingCartService;
import com.qairline.qairline_backend.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketShoppingCartServiceImpl implements TicketShoppingCartService {
    private final TicketShoppingCartRepository ticketShoppingCartRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public TicketShoppingCart addTicketToCart(TicketCartAddDTO dto) {
        TicketShoppingCart ticket = TicketShoppingCart.builder()
                .customer(dto.getCustomer())
                .flight(dto.getFlight())
                .seatType(dto.getSeatType().equals(SeatType.BUSINESS.name()) ? SeatType.BUSINESS.name() : SeatType.ECONOMY.name())
                .createdTime(new Date())
                .build();

        ticket = ticketShoppingCartRepository.save(ticket);
        return ticket;
    }

    @Override
    public TicketShoppingCart getTicketCart(String id) {
        TicketShoppingCart ticket = ticketShoppingCartRepository.findById(id).orElse(null);

        if(ticket == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Ticket cart does not exist");
        }

        return ticket;
    }

    @Override
    public List<TicketShoppingCart> getTicketCartByCustomer() {
        return ticketShoppingCartRepository.findByCustomer(((User)AuthenticationUtils.getCurrentUser()).getId());
    }

    @Override
    public void editTicketCart(TicketCartEditDTO dto) {
        TicketShoppingCart ticket = ticketShoppingCartRepository.findById(dto.getId()).orElse(null);

        if(ticket == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Ticket cart does not exist");
        }

        ticket.setName(dto.getName());
        ticket.setBirthDay(dto.getBirthDay());
        ticket.setAddress(dto.getAddress());
        ticket.setEmail(dto.getEmail());
        ticket.setPhone(dto.getPhone());
        ticket.setIdCard(dto.getIdCard());
        ticketShoppingCartRepository.save(ticket);
    }

    @Override
    public void removeTicketFromCart(String id) {
        TicketShoppingCart ticket = ticketShoppingCartRepository.findById(id).orElse(null);

        if(ticket == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Ticket cart does not exist");
        }

        ticketShoppingCartRepository.delete(ticket);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOldTicketsFromShoppingCart() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -5);

        Date timeLimit = calendar.getTime();

        Query query = new Query();
        query.addCriteria(Criteria.where("createdTime").lte(timeLimit));

        List<TicketShoppingCart> oldTickets = mongoTemplate.find(query, TicketShoppingCart.class);

        oldTickets.forEach(mongoTemplate::remove);
    }
}
