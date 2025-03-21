package com.qairline.qairline_backend.flight.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document("airport")
@AllArgsConstructor
@NoArgsConstructor
public class Airport {
    @Id
    private String id;

    @Indexed(background = true, unique = true)
    private String airportCode;

    private String airportName;
    private String cityId;
    private String location;
}
