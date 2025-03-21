package com.qairline.qairline_backend.scheduled_flight.controller;

import com.qairline.qairline_backend.common.api.ApiResponse;
import com.qairline.qairline_backend.scheduled_flight.dto.ScheduledFlightEditRequest;
import com.qairline.qairline_backend.scheduled_flight.dto.ScheduledFlightRequestDTO;
import com.qairline.qairline_backend.scheduled_flight.dto.ScheduledFlightResponseDTO;
import com.qairline.qairline_backend.scheduled_flight.service.ScheduledFlightService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "scheduled_flight")
@RequestMapping("/api/admin/v1/scheduled_flights")
public class ScheduledFlightController {
    @Value("${api.token}")
    private String apiToken;

    private final ScheduledFlightService flightService;

    @PostMapping
    public ResponseEntity<ApiResponse> addScheduledFlight(@RequestHeader("X-auth-token") String token,
                                                 @RequestBody ScheduledFlightRequestDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            flightService.addScheduledFlight(dto);
            return ResponseEntity.ok(ApiResponse.success("Add scheduled flight successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<ApiResponse> editScheduledFlight(@RequestHeader("X-auth-token") String token,
                                                  @RequestBody ScheduledFlightEditRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            flightService.editScheduledFlight(request);
            return ResponseEntity.ok(ApiResponse.success("Edit scheduled flight successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/open")
    public ResponseEntity<ApiResponse> openScheduledFlight(@RequestHeader("X-auth-token") String token, @RequestBody String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            flightService.openScheduledFlight(id);
            return ResponseEntity.ok(ApiResponse.success("Open scheduled flight successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/close")
    public ResponseEntity<ApiResponse> closeScheduledFlight(@RequestHeader("X-auth-token") String token, @RequestBody String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            flightService.closeScheduledFlight(id);
            return ResponseEntity.ok(ApiResponse.success("Close scheduled flight successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteScheduledFlight(@RequestHeader("X-auth-token") String token, @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            flightService.deleteScheduledFlight(id);
            return ResponseEntity.ok(ApiResponse.success("Delete scheduled flight successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ScheduledFlightResponseDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAllScheduledFlights(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            List<ScheduledFlightResponseDTO> flights = flightService.getAllScheduledFlights();
            return ResponseEntity.ok(ApiResponse.success("Get all scheduled flights successful", flights));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_by_id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ScheduledFlightResponseDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getScheduledFlightById(@RequestHeader("X-auth-token") String token, @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            ScheduledFlightResponseDTO flight = flightService.getScheduledFlightById(id);
            return ResponseEntity.ok(ApiResponse.success("Get scheduled flight by id successful", flight));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
