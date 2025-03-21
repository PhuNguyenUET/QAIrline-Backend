package com.qairline.qairline_backend.flight.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.qairline.qairline_backend.common.exception.BusinessException;
import com.qairline.qairline_backend.flight.dto.AirportAddDTO;
import com.qairline.qairline_backend.flight.dto.AirportResponseDTO;
import com.qairline.qairline_backend.flight.dto.FlightResponseDTO;
import com.qairline.qairline_backend.flight.model.Airport;
import com.qairline.qairline_backend.flight.model.City;
import com.qairline.qairline_backend.flight.model.Flight;
import com.qairline.qairline_backend.flight.repository.AirportRepository;
import com.qairline.qairline_backend.flight.service.AirportService;
import com.qairline.qairline_backend.flight.service.CityService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AirportServiceImpl implements AirportService {
    private final AirportRepository airportRepository;
    private final CityService cityService;
    private ModelMapper modelMapper;

    private final Set<City> cities = new HashSet<>();

    private final Cache<String, Airport> airportCache =
            CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(45, TimeUnit.MINUTES)
            .build();

    @PostConstruct
    private void setUpModelMapper() {
        modelMapper = new ModelMapper();

        modelMapper.createTypeMap(Airport.class, AirportResponseDTO.class)
                .setConverter(context -> {
                    Airport airport = context.getSource();
                    return AirportResponseDTO.builder()
                            .id(airport.getId())
                            .airportCode(airport.getAirportCode())
                            .airportName(airport.getAirportName())
                            .city(cityService.findById(airport.getCityId()))
                            .location(airport.getLocation())
                            .build();
                });
    }

    @Override
    public List<AirportResponseDTO> getAirportsByCity(String cityId) {
        return airportRepository.findByCityId(cityId).stream().map(airport -> modelMapper.map(airport, AirportResponseDTO.class)).toList();
    }

    @Override
    public List<AirportResponseDTO> getAllAirports() {
        return airportRepository.findAll().stream().map(airport -> modelMapper.map(airport, AirportResponseDTO.class)).toList();
    }

    @Override
    public List<City> getAllCities() {
        if(cities.size() < 20) {
            cities.clear();
            List<City> citiesRepo = cityService.findAll();

            cities.addAll(citiesRepo);
        }

        return cities.stream().toList();
    }

    @Override
    public Airport findById(String id) {
        if(airportCache.getIfPresent(id) != null) {
            return airportCache.getIfPresent(id);
        }

        Airport airport = airportRepository.findById(id).orElse(null);

        if(airport == null) {
            return null;
        } else {
            airportCache.put(id, airport);
            return airport;
        }
    }

    @Override
    public void addAirport(AirportAddDTO dto) {
        Airport airport = Airport.builder()
                .airportName(dto.getName())
                .location(dto.getLocation())
                .cityId(dto.getCityId())
                .build();

        cities.add(cityService.findById(dto.getCityId()));

        airportRepository.save(airport);
    }

    @Override
    public void editAirport(Airport dto) {
        Airport airport = airportRepository.findById(dto.getId()).orElse(null);

        if(airport == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Airport does not exist");
        }

        cities.add(cityService.findById(dto.getCityId()));

        airportCache.put(dto.getId(), dto);
        airportRepository.save(dto);
    }

    @Override
    public void deleteAirport(String id) {
        Airport airport = airportRepository.findById(id).orElse(null);

        if(airport == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Airport does not exist");
        }

        airportCache.invalidate(id);

        airportRepository.delete(airport);
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void refreshCities() {
        cities.clear();

        List<Airport> airports = airportRepository.findAll();
        airports.forEach(airport -> cities.add(cityService.findById(airport.getCityId())));
    }
}
