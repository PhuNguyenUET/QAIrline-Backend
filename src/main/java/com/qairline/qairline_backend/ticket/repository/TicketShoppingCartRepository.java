package com.qairline.qairline_backend.ticket.repository;

import com.qairline.qairline_backend.ticket.model.TicketShoppingCart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TicketShoppingCartRepository extends MongoRepository<TicketShoppingCart, String> {
    List<TicketShoppingCart> findByCustomer(String customerId);
}
