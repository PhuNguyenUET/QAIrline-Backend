package com.qairline.qairline_backend.flight.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AirportAddDTO {
    private String name;
    private String airportCode;
    private String cityId;
    private String location;
}
