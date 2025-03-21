package com.qairline.qairline_backend.scheduled_flight.service;

import com.qairline.qairline_backend.scheduled_flight.dto.ScheduledFlightEditRequest;
import com.qairline.qairline_backend.scheduled_flight.dto.ScheduledFlightRequestDTO;
import com.qairline.qairline_backend.scheduled_flight.dto.ScheduledFlightResponseDTO;
import com.qairline.qairline_backend.scheduled_flight.model.ScheduledFlight;

import java.util.List;

public interface ScheduledFlightService {
    List<ScheduledFlightResponseDTO> getAllScheduledFlights();
    ScheduledFlight findById(String id);
    ScheduledFlightResponseDTO getScheduledFlightById(String id);

    void addScheduledFlight(ScheduledFlightRequestDTO dto);
    void editScheduledFlight(ScheduledFlightEditRequest request);
    void deleteScheduledFlight(String id);
    void openScheduledFlight(String id);
    void closeScheduledFlight(String id);
}
