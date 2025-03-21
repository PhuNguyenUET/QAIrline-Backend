package com.qairline.qairline_backend.flight.service;

import com.qairline.qairline_backend.flight.dto.CityAddDTO;
import com.qairline.qairline_backend.flight.model.City;

import java.util.List;

public interface CityService {
    City findById(String id);
    List<City> findAll();
    City findByCityCode(String cityCode);
    void addCity(CityAddDTO dto);
    void editCity(City city);
    void deleteCity(String id);
}
