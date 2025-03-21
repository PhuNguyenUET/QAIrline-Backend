package com.qairline.qairline_backend.flight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qairline.qairline_backend.flight.model.Airport;
import com.qairline.qairline_backend.flight.model.Plane;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class FlightResponseDTO {
    private String id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date departureTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date arrivalTime;

    private AirportResponseDTO originAirport;
    private AirportResponseDTO destinationAirport;

    private boolean delayed;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date delayedDepartureTime;
    private String flightNumber;

    private int availableEconomySeats;
    private int availableBusinessSeats;
    private int economyPrice;
    private int businessPrice;
    private double discountPercent;
    private boolean isAvailable;
    private Plane plane;
}
