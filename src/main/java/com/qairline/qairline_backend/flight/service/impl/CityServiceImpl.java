package com.qairline.qairline_backend.flight.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.qairline.qairline_backend.common.exception.BusinessException;
import com.qairline.qairline_backend.flight.dto.CityAddDTO;
import com.qairline.qairline_backend.flight.model.City;
import com.qairline.qairline_backend.flight.repository.CityRepository;
import com.qairline.qairline_backend.flight.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {
    private final CityRepository cityRepository;

    private final Cache<String, City> cityIdCache = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(45, TimeUnit.MINUTES)
            .build();

    private final Cache<String, City> cityCodeCache = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(45, TimeUnit.MINUTES)
            .build();

    @Override
    public City findById(String id) {
        if(cityIdCache.getIfPresent(id) != null) {
            return cityIdCache.getIfPresent(id);
        }

        City city = cityRepository.findById(id).orElse(null);

        if(city == null) {
            return null;
        } else {
            cityIdCache.put(id, city);
            return city;
        }
    }

    @Override
    public List<City> findAll() {
        return cityRepository.findAll();
    }

    @Override
    public City findByCityCode(String cityCode) {
        if(cityCodeCache.getIfPresent(cityCode) != null) {
            return cityCodeCache.getIfPresent(cityCode);
        }

        City city = cityRepository.findByCityCode(cityCode).orElse(null);

        if(city == null) {
            return null;
        } else {
            cityCodeCache.put(cityCode, city);
            return city;
        }
    }

    @Override
    public void addCity(CityAddDTO dto) {
        City city = City.builder()
                .cityCode(dto.getCityCode())
                .cityName(dto.getCityName())
                .build();

        cityRepository.save(city);
    }

    @Override
    public void editCity(City dto) {
        City city = cityRepository.findById(dto.getId()).orElse(null);

        if(city == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "City does not exist");
        }

        cityRepository.save(dto);
        cityIdCache.put(dto.getId(), dto);
    }

    @Override
    public void deleteCity(String id) {
        City city = cityRepository.findById(id).orElse(null);

        if(city == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "City does not exist");
        }

        cityRepository.delete(city);
        cityIdCache.invalidate(id);
    }
}
