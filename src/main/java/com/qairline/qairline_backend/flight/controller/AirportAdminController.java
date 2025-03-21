package com.qairline.qairline_backend.flight.controller;

import com.qairline.qairline_backend.common.api.ApiResponse;
import com.qairline.qairline_backend.flight.dto.AirportAddDTO;
import com.qairline.qairline_backend.flight.dto.AirportResponseDTO;
import com.qairline.qairline_backend.flight.dto.CityAddDTO;
import com.qairline.qairline_backend.flight.model.Airport;
import com.qairline.qairline_backend.flight.model.City;
import com.qairline.qairline_backend.flight.service.AirportService;
import com.qairline.qairline_backend.flight.service.CityService;
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
@Tag(name = "airport_admin")
@RequestMapping("/api/admin/v1/airports")
public class AirportAdminController {
    @Value("${api.token}")
    private String apiToken;

    private final AirportService airportService;
    private final CityService cityService;

    @PostMapping
    public ResponseEntity<ApiResponse> addAirport(@RequestHeader("X-auth-token") String token, @RequestBody AirportAddDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            airportService.addAirport(dto);
            return ResponseEntity.ok(ApiResponse.success("Add airport successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<ApiResponse> editAirport(@RequestHeader("X-auth-token") String token, @RequestBody Airport airport) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            airportService.editAirport(airport);
            return ResponseEntity.ok(ApiResponse.success("Edit airport successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/cities")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = String.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAllCities(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            List<City> cities = airportService.getAllCities();
            return ResponseEntity.ok(ApiResponse.success("Get all cities successful", cities));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteAirport(@RequestHeader("X-auth-token") String token, @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            airportService.deleteAirport(id);
            return ResponseEntity.ok(ApiResponse.success("Delete airport successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = AirportResponseDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAllAirports(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            List<AirportResponseDTO> airports = airportService.getAllAirports();
            return ResponseEntity.ok(ApiResponse.success("Get all airports successful", airports));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/find_by_city")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = AirportResponseDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAirportsByCity(@RequestHeader("X-auth-token") String token, @RequestParam String cityId) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            List<AirportResponseDTO> airports = airportService.getAirportsByCity(cityId);
            return ResponseEntity.ok(ApiResponse.success("Get airports by city successful", airports));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/cities")
    public ResponseEntity<ApiResponse> addCity(@RequestHeader("X-auth-token") String token, @RequestBody CityAddDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            cityService.addCity(dto);
            return ResponseEntity.ok(ApiResponse.success("Add city successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/cities")
    public ResponseEntity<ApiResponse> editCity(@RequestHeader("X-auth-token") String token, @RequestBody City dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            cityService.editCity(dto);
            return ResponseEntity.ok(ApiResponse.success("Add city successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping("/cities")
    public ResponseEntity<ApiResponse> deleteCity(@RequestHeader("X-auth-token") String token, @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            cityService.deleteCity(id);
            return ResponseEntity.ok(ApiResponse.success("Add city successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
