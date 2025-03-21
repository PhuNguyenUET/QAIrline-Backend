package com.qairline.qairline_backend.ticket.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("ticket_shopping_cart")
public class TicketShoppingCart {
    @Id
    private String id;

    @Indexed(background = true)
    private String customer;

    private String flight;

    private String name;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date birthDay;
    private String address;
    private String email;
    private String phone;

    private String idCard;

    private String seatType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date createdTime;
}
