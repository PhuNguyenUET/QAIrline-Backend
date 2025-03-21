package com.qairline.qairline_backend.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class TicketCartAddDTO {
    private String customer;
    private String flight;
    private String seatType;
}
