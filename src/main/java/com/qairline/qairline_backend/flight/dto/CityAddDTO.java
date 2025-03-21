package com.qairline.qairline_backend.flight.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CityAddDTO {
    String cityCode;
    String cityName;
}
