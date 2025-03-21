package com.qairline.qairline_backend.ticket.controller;

import com.qairline.qairline_backend.common.api.ApiResponse;
import com.qairline.qairline_backend.ticket.dto.TicketEditRequestDTO;
import com.qairline.qairline_backend.ticket.dto.TicketResponseDTO;
import com.qairline.qairline_backend.ticket.service.TicketService;
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
@Tag(name = "ticket_admin")
@RequestMapping("/api/admin/v1/booking")
public class TicketAdminController {
    @Value("${api.token}")
    private String apiToken;

    private final TicketService ticketService;

    @GetMapping("/filter")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = TicketResponseDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> filterTickets(@RequestHeader("X-auth-token") String token,
                                                     @RequestParam(required = false) String departureCity,
                                                     @RequestParam(required = false) String destinationCity,
                                                     @RequestParam(required = false) String departureAirport,
                                                     @RequestParam(required = false) String destinationAirport,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") Date departureTimeStart,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") Date departureTimeEnd,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") Date bookingTimeStart,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") Date bookingTimeEnd
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
            filterMap.put("bookingTimeStart", bookingTimeStart);
            filterMap.put("bookingTimeEnd", bookingTimeEnd);

            List<TicketResponseDTO> flights = ticketService.filterTicket(filterMap);
            return ResponseEntity.ok(ApiResponse.success("Get filter tickets successful", flights));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/search")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = TicketResponseDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getBookingById(@RequestHeader("X-auth-token") String token, @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            TicketResponseDTO ticket = ticketService.getTicketById(id);
            return ResponseEntity.ok(ApiResponse.success("Get booking successful", ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteBooking(@RequestHeader("X-auth-token") String token, @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            ticketService.deleteTicket(id);
            return ResponseEntity.ok(ApiResponse.success("Delete ticket successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = TicketResponseDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> editBooking(@RequestHeader("X-auth-token") String token, @RequestBody TicketEditRequestDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            TicketResponseDTO response = ticketService.editTicket(dto);
            return ResponseEntity.ok(ApiResponse.success("Edit booking successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
