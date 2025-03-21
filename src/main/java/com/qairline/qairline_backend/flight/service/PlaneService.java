package com.qairline.qairline_backend.flight.service;

import com.qairline.qairline_backend.flight.dto.PlaneAddDTO;
import com.qairline.qairline_backend.flight.model.Plane;

import java.util.List;

public interface PlaneService {
    List<Plane> getAllPlanes();

    Plane findById(String id);

    void addPlane(PlaneAddDTO dto);
    void editPlane(Plane plane);
    void deletePLane(String id);
}
