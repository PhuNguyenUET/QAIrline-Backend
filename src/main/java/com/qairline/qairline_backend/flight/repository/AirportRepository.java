package com.qairline.qairline_backend.flight.repository;

import com.qairline.qairline_backend.flight.model.Airport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface AirportRepository extends MongoRepository<Airport, String> {
    List<Airport> findByCityId(String cityId);
}
