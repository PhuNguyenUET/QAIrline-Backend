package com.qairline.qairline_backend.flight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qairline.qairline_backend.scheduled_flight.model.DayOfWeek;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class FlightRequestBatchDTO {
    private String originAirport;
    private String destinationAirport;

    private String plane;

    private DayOfWeek dayOfWeekDeparture;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private Date departureTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private Date arrivalTime;

    private int economyPrice;
    private int businessPrice;
    private double discountPercent;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date addFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date addUntil;
}
