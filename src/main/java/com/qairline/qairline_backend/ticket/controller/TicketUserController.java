package com.qairline.qairline_backend.ticket.controller;

import com.qairline.qairline_backend.common.api.ApiResponse;
import com.qairline.qairline_backend.flight.dto.FlightResponseDTO;
import com.qairline.qairline_backend.ticket.dto.TicketAddRequestDTO;
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
@Tag(name = "ticket_user")
@RequestMapping("/api/customer/v1/booking")
public class TicketUserController {
    @Value("${api.token}")
    private String apiToken;

    private final TicketService ticketService;

    @PostMapping
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = TicketResponseDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> addBooking(@RequestHeader("X-auth-token") String token, @RequestBody TicketAddRequestDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            TicketResponseDTO response = ticketService.bookTicket(dto);
            return ResponseEntity.ok(ApiResponse.success("Add booking successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/batch")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = TicketResponseDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> addBookingBatch(@RequestHeader("X-auth-token") String token, @RequestBody List<TicketAddRequestDTO> dtoList) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            List<TicketResponseDTO> response = ticketService.bookTicketBatch(dtoList);
            return ResponseEntity.ok(ApiResponse.success("Add booking successfully", response));
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

    @GetMapping
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = TicketResponseDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAllBookings(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            List<TicketResponseDTO> tickets = ticketService.getAllTickets();
            return ResponseEntity.ok(ApiResponse.success("Get all bookings successfully", tickets));
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

    @PostMapping("/pay")
    public ResponseEntity<ApiResponse> makePayment(@RequestHeader("X-auth-token") String token, @RequestBody String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            ticketService.makePayment(id);
            return ResponseEntity.ok(ApiResponse.success("Make payment successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/pay/batch")
    public ResponseEntity<ApiResponse> makePaymentBatch(@RequestHeader("X-auth-token") String token, @RequestBody List<String> tickets) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            ticketService.makePaymentBatch(tickets);
            return ResponseEntity.ok(ApiResponse.success("Make payment successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

}
