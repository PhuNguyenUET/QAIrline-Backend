package com.qairline.qairline_backend.flight.controller;

import com.qairline.qairline_backend.authentication.dto.AuthenticationResponse;
import com.qairline.qairline_backend.common.api.ApiResponse;
import com.qairline.qairline_backend.flight.dto.PlaneAddDTO;
import com.qairline.qairline_backend.flight.model.Plane;
import com.qairline.qairline_backend.flight.service.PlaneService;
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
@Tag(name = "plane")
@RequestMapping("/api/admin/v1/planes")
public class PlaneController {
    @Value("${api.token}")
    private String apiToken;
    
    private final PlaneService planeService;

    @PostMapping
    public ResponseEntity<ApiResponse> addPlane(@RequestHeader("X-auth-token") String token, @RequestBody PlaneAddDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            planeService.addPlane(dto);
            return ResponseEntity.ok(ApiResponse.success("Add plane successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<ApiResponse> editPlane(@RequestHeader("X-auth-token") String token, @RequestBody Plane plane) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            planeService.editPlane(plane);
            return ResponseEntity.ok(ApiResponse.success("Edit plane successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> deletePlane(@RequestHeader("X-auth-token") String token, @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            planeService.deletePLane(id);
            return ResponseEntity.ok(ApiResponse.success("Delete plane successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = Plane.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAllPlanes(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            List<Plane> planes = planeService.getAllPlanes();
            return ResponseEntity.ok(ApiResponse.success("Get all planes successful", planes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
