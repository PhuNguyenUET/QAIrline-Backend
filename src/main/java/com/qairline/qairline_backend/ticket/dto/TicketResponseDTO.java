package com.qairline.qairline_backend.ticket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qairline.qairline_backend.flight.dto.FlightResponseDTO;
import com.qairline.qairline_backend.flight.model.Flight;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketResponseDTO {
    private String id;
    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private Date birthday;
    private String address;
    private String email;
    private String phone;
    private FlightResponseDTO flight;
    private String passengerId;

    private String seat;
    private String seatType;

    private int price;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date allowChangeUntil;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date allowPaymentUntil;
}
