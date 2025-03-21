package com.qairline.qairline_backend.flight.repository;

import com.qairline.qairline_backend.flight.model.Flight;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

public interface FlightRepository extends MongoRepository<Flight, String> {
}
