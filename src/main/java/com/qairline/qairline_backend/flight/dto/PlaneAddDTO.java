package com.qairline.qairline_backend.flight.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlaneAddDTO {
    private String brand;
    private String model;
    private int numOfEconomySeats;
    private int numOfBusinessSeats;
}
