package com.qairline.qairline_backend.scheduled_flight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qairline.qairline_backend.scheduled_flight.model.DayOfWeek;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ScheduledFlightRequestDTO {
    private String originAirport;
    private String destinationAirport;

    private DayOfWeek dayOfWeekDeparture;

    private String plane;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private Date departureTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private Date arrivalTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date validUntil;

    private int economyPrice;
    private int businessPrice;
    private double discountPercent;

    private boolean available;
}
