package com.qairline.qairline_backend.flight.repository;

import com.qairline.qairline_backend.flight.model.City;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CityRepository extends MongoRepository<City, String> {
    Optional<City> findByCityCode(String cityCode);
}
