package com.qairline.qairline_backend.flight.service;

import com.qairline.qairline_backend.flight.dto.FlightEditRequest;
import com.qairline.qairline_backend.flight.dto.FlightRequestBatchDTO;
import com.qairline.qairline_backend.flight.dto.FlightRequestDTO;
import com.qairline.qairline_backend.flight.dto.FlightResponseDTO;
import com.qairline.qairline_backend.flight.model.Flight;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface FlightService {
    List<FlightResponseDTO> getAllFlights();
    List<FlightResponseDTO> filterFlights(Map<String, Object> filterMap);
    Flight findById(String id);
    FlightResponseDTO getFlightById(String id);
    void bookFlight(String id, String seatType);
    void removeBookFlight(String id, String seatType);

    void addFlight(FlightRequestDTO dto);
    void addFlightBatch(FlightRequestBatchDTO dto);
    void editFlight(FlightEditRequest request);
    void deleteFlight(String id);
    void openFlight(String id);
    void closeFlight(String id);
    void delayFlight(String id, Date newDepartureTime);
}
