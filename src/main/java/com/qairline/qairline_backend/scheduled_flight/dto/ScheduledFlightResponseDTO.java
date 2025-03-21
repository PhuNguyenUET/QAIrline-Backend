package com.qairline.qairline_backend.scheduled_flight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qairline.qairline_backend.flight.model.Airport;
import com.qairline.qairline_backend.flight.model.Plane;
import com.qairline.qairline_backend.scheduled_flight.model.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ScheduledFlightResponseDTO {
    private String id;

    private DayOfWeek dayOfWeekDeparture;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private Date departureTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private Date arrivalTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date validUntil;

    private Airport originAirport;
    private Airport destinationAirport;

    private int availableEconomySeats;
    private int availableBusinessSeats;
    private int economyPrice;
    private int businessPrice;
    private double discountPercent;
    private boolean isAvailable;
    private Plane plane;
}
