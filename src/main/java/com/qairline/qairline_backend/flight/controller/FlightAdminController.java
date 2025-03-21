package com.qairline.qairline_backend.flight.controller;

import com.qairline.qairline_backend.authentication.dto.AuthenticationResponse;
import com.qairline.qairline_backend.client.model.Role;
import com.qairline.qairline_backend.common.api.ApiResponse;
import com.qairline.qairline_backend.flight.dto.*;
import com.qairline.qairline_backend.flight.service.FlightService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "flight_admin")
@RequestMapping("/api/admin/v1/flights")
public class FlightAdminController {
    @Value("${api.token}")
    private String apiToken;

    private final FlightService flightService;

    @PostMapping
    public ResponseEntity<ApiResponse> addFlight(@RequestHeader("X-auth-token") String token,
                                                     @RequestBody FlightRequestDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            flightService.addFlight(dto);
            return ResponseEntity.ok(ApiResponse.success("Add flight successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<ApiResponse> editFlight(@RequestHeader("X-auth-token") String token,
                                                     @RequestBody FlightEditRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            flightService.editFlight(request);
            return ResponseEntity.ok(ApiResponse.success("Edit flight successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/open")
    public ResponseEntity<ApiResponse> openFlight(@RequestHeader("X-auth-token") String token, @RequestBody String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            flightService.openFlight(id);
            return ResponseEntity.ok(ApiResponse.success("Open flight successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/close")
    public ResponseEntity<ApiResponse> closeFlight(@RequestHeader("X-auth-token") String token, @RequestBody String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            flightService.closeFlight(id);
            return ResponseEntity.ok(ApiResponse.success("Close flight successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/delay")
    public ResponseEntity<ApiResponse> delayFlight(@RequestHeader("X-auth-token") String token, @RequestBody DelayDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            flightService.delayFlight(dto.getId(), dto.getNewDepartureTime());
            return ResponseEntity.ok(ApiResponse.success("Delay flight successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteFlight(@RequestHeader("X-auth-token") String token, @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            flightService.deleteFlight(id);
            return ResponseEntity.ok(ApiResponse.success("Delete flight successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = FlightResponseDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAllFlights(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            List<FlightResponseDTO> flights = flightService.getAllFlights();
            return ResponseEntity.ok(ApiResponse.success("Get all flights successful", flights));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_by_id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = FlightResponseDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getFlightById(@RequestHeader("X-auth-token") String token, @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            FlightResponseDTO flight = flightService.getFlightById(id);
            return ResponseEntity.ok(ApiResponse.success("Get flight by id successful", flight));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/filter")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = FlightResponseDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> filterFlights(@RequestHeader("X-auth-token") String token,
                                                     @RequestParam(required = false) String departureCity,
                                                     @RequestParam(required = false) String destinationCity,
                                                     @RequestParam(required = false) String departureAirport,
                                                     @RequestParam(required = false) String destinationAirport,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") Date departureTimeStart,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") Date departureTimeEnd,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") Date destinationTimeStart,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") Date destinationTimeEnd,
                                                     @RequestParam(required = false) String princeRange,
                                                     @RequestParam(required = false) String seatType
    ) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");

            Map<String, Object> filterMap = new HashMap<>();

            filterMap.put("destinationCity", destinationCity);
            filterMap.put("departureCity", departureCity);
            filterMap.put("departureAirport", departureAirport);
            filterMap.put("destinationAirport", destinationAirport);
            filterMap.put("departureTimeStart", departureTimeStart);
            filterMap.put("departureTimeEnd", departureTimeEnd);
            filterMap.put("destinationTimeStart", destinationTimeStart);
            filterMap.put("destinationTimeEnd", destinationTimeEnd);
            filterMap.put("princeRange", princeRange);
            filterMap.put("seatType", seatType);
            filterMap.put("role", Role.ADMIN);

            List<FlightResponseDTO> flights = flightService.filterFlights(filterMap);
            return ResponseEntity.ok(ApiResponse.success("Get filter flights successful", flights));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/add_batch")
    public ResponseEntity<ApiResponse> addFlightBatch(@RequestHeader("X-auth-token") String token,
                                                     @RequestBody FlightRequestBatchDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            flightService.addFlightBatch(dto);
            return ResponseEntity.ok(ApiResponse.success("Add flight batch successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
