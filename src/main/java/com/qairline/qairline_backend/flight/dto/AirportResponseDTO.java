package com.qairline.qairline_backend.flight.dto;

import com.qairline.qairline_backend.flight.model.City;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AirportResponseDTO {
    private String id;

    private String airportCode;

    private String airportName;
    private City city;
    private String location;
}
