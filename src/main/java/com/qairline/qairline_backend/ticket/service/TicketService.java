package com.qairline.qairline_backend.ticket.service;

import com.qairline.qairline_backend.flight.dto.FlightResponseDTO;
import com.qairline.qairline_backend.ticket.dto.TicketAddRequestDTO;
import com.qairline.qairline_backend.ticket.dto.TicketEditRequestDTO;
import com.qairline.qairline_backend.ticket.dto.TicketResponseDTO;
import com.qairline.qairline_backend.ticket.model.Ticket;

import java.util.List;
import java.util.Map;

public interface TicketService {
    List<TicketResponseDTO> getAllTickets();

    TicketResponseDTO getTicketById(String id);

    TicketResponseDTO bookTicket(TicketAddRequestDTO dto);
    TicketResponseDTO editTicket(TicketEditRequestDTO dto);

    List<TicketResponseDTO> bookTicketBatch(List<TicketAddRequestDTO> dtoList);

    List<TicketResponseDTO> filterTicket (Map<String, Object> filterMap);

    void cancelTicket(String id);
    void deleteTicket(String id);

    void makePayment(String id);
    void makePaymentBatch(List<String> tickets);
    void refund(double amount, String email);
}
