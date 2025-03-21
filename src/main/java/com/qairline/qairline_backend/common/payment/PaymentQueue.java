package com.qairline.qairline_backend.common.payment;

import com.qairline.qairline_backend.flight.service.FlightService;
import com.qairline.qairline_backend.ticket.model.Ticket;
import com.qairline.qairline_backend.ticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class PaymentQueue {
    private static PaymentQueue instance;

    PaymentQueue() {}

    public static PaymentQueue getInstance() {
        if(instance == null) {
            instance = new PaymentQueue();
        }

        return instance;
    }

    @Autowired
    private TicketService ticketService;

    @Autowired
    private FlightService flightService;

    private final ConcurrentHashMap<String, TicketPayment> unpaidTicketQueue = new ConcurrentHashMap<>();

    @Scheduled(cron = "0 */10 * * * *")
    public void checkForExpiredTicket() {
        for(Map.Entry<String, TicketPayment> entry : unpaidTicketQueue.entrySet()) {
            if(entry.getValue().getTicket().getAllowedPaymentUntil().before(new Date())) {
                ticketService.cancelTicket(entry.getValue().getTicket().getId());
                removeFromPaymentQueue(entry.getKey(), entry.getValue());
            }
        }
    }

    public void addToPaymentQueue(String userId, Ticket ticket, boolean needRefund, int refundAmount) {
        String paymentId = userId + new Date().getTime();
        TicketPayment ticketPayment = new TicketPayment(userId, ticket, needRefund, refundAmount);
        unpaidTicketQueue.put(paymentId, ticketPayment);
    }

    public void removeFromPaymentQueue(String paymentId, TicketPayment ticketPayment) {
        flightService.removeBookFlight(ticketPayment.getTicket().getFlight(), ticketPayment.getTicket().getSeatType());
        unpaidTicketQueue.remove(paymentId);

        if(ticketPayment.isNeedRefund()) {
            ticketService.refund(ticketPayment.getRefundAmount(), ticketPayment.getTicket().getEmail());
        }
    }
}
