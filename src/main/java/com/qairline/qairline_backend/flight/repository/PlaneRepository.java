package com.qairline.qairline_backend.flight.repository;

import com.qairline.qairline_backend.flight.model.Plane;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

public interface PlaneRepository extends MongoRepository<Plane, String> {
}
