package com.qairline.qairline_backend.flight.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document("plane")
@NoArgsConstructor
@AllArgsConstructor
public class Plane {
    @Id
    private String id;

    private String brand;

    private String model;

    private int numOfEconomySeats;
    private int numOfBusinessSeats;
}
