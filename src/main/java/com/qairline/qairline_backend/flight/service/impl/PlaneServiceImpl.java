package com.qairline.qairline_backend.flight.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.qairline.qairline_backend.common.exception.BusinessException;
import com.qairline.qairline_backend.flight.dto.PlaneAddDTO;
import com.qairline.qairline_backend.flight.model.Plane;
import com.qairline.qairline_backend.flight.repository.PlaneRepository;
import com.qairline.qairline_backend.flight.service.PlaneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PlaneServiceImpl implements PlaneService {
    private final PlaneRepository planeRepository;

    private final Cache<String, Plane> planeCache =
            CacheBuilder.newBuilder()
                    .maximumSize(500)
                    .expireAfterAccess(45, TimeUnit.MINUTES)
                    .build();;

    @Override
    public List<Plane> getAllPlanes() {
        return planeRepository.findAll();
    }

    @Override
    public Plane findById(String id) {
        if(planeCache.getIfPresent(id) != null) {
            return planeCache.getIfPresent(id);
        }

        Plane plane = planeRepository.findById(id).orElse(null);

        if(plane == null) {
            return null;
        } else {
            planeCache.put(id, plane);
            return plane;
        }
    }

    @Override
    public void addPlane(PlaneAddDTO dto) {
        Plane plane = Plane.builder()
                .brand(dto.getBrand())
                .model(dto.getModel())
                .numOfEconomySeats(dto.getNumOfEconomySeats())
                .numOfBusinessSeats(dto.getNumOfBusinessSeats())
                .build();

        planeRepository.save(plane);
    }

    @Override
    public void editPlane(Plane dto) {
        Plane plane = planeRepository.findById(dto.getId()).orElse(null);

        if(plane == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Plane does not exist");
        } else {
            planeCache.put(dto.getId(), dto);
            planeRepository.save(dto);
        }
    }

    @Override
    public void deletePLane(String id) {
        Plane plane = planeRepository.findById(id).orElse(null);

        if(plane == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Plane does not exist");
        }

        planeCache.invalidate(id);

        planeRepository.delete(plane);
    }
}
