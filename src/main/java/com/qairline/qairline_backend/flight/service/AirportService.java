package com.qairline.qairline_backend.flight.service;

import com.qairline.qairline_backend.flight.dto.AirportAddDTO;
import com.qairline.qairline_backend.flight.dto.AirportResponseDTO;
import com.qairline.qairline_backend.flight.model.Airport;
import com.qairline.qairline_backend.flight.model.City;

import java.util.List;

public interface AirportService {
    List<AirportResponseDTO> getAirportsByCity(String cityId);
    List<AirportResponseDTO> getAllAirports();
    List<City> getAllCities();

    Airport findById(String id);

    void addAirport(AirportAddDTO dto);
    void editAirport(Airport dto);
    void deleteAirport(String id);
}
