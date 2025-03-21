package com.qairline.qairline_backend.ticket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TicketAddRequestDTO {
    private String flight;

    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private Date birthDay;
    private String address;
    private String phone;
    private String email;

    private String ticketShoppingCart;

    private String idCard;

    private String seat;
    private String seatType;
}
