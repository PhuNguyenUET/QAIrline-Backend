package com.qairline.qairline_backend.ticket.service;

import com.qairline.qairline_backend.ticket.dto.TicketCartAddDTO;
import com.qairline.qairline_backend.ticket.dto.TicketCartEditDTO;
import com.qairline.qairline_backend.ticket.model.TicketShoppingCart;

import java.util.List;

public interface TicketShoppingCartService {
    TicketShoppingCart addTicketToCart(TicketCartAddDTO dto);
    TicketShoppingCart getTicketCart(String id);
    List<TicketShoppingCart> getTicketCartByCustomer();
    void editTicketCart(TicketCartEditDTO dto);
    void removeTicketFromCart(String id);
}
