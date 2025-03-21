package com.qairline.qairline_backend.flight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class FlightRequestDTO {
    private String originAirport;
    private String destinationAirport;

    private String plane;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date departureTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date arrivalTime;

    private int economyPrice;
    private int businessPrice;
    private double discountPercent;

    private boolean available;
}
