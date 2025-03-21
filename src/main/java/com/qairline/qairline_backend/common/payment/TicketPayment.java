package com.qairline.qairline_backend.common.payment;

import com.qairline.qairline_backend.ticket.model.Ticket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketPayment {
    private String userId;
    private Ticket ticket;
    private boolean needRefund;
    private int refundAmount;
}
