package com.qairline.qairline_backend.ticket.controller;

import com.qairline.qairline_backend.common.api.ApiResponse;
import com.qairline.qairline_backend.ticket.dto.TicketCartAddDTO;
import com.qairline.qairline_backend.ticket.dto.TicketCartEditDTO;
import com.qairline.qairline_backend.ticket.model.TicketShoppingCart;
import com.qairline.qairline_backend.ticket.service.TicketShoppingCartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "ticket_shopping_cart")
@RequestMapping("/api/customer/v1/booking/shopping_cart")
public class TicketShoppingCartController {
    @Value("${api.token}")
    private String apiToken;

    private final TicketShoppingCartService ticketShoppingCartService;

    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteTicket(@RequestHeader("X-auth-token") String token, @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            ticketShoppingCartService.removeTicketFromCart(id);
            return ResponseEntity.ok(ApiResponse.success("Delete ticket successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addTicket(@RequestHeader("X-auth-token") String token, @RequestBody TicketCartAddDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            TicketShoppingCart ticket = ticketShoppingCartService.addTicketToCart(dto);
            return ResponseEntity.ok(ApiResponse.success("Add ticket successful", ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_by_customer")
    public ResponseEntity<ApiResponse> getTicketCartByCustomer(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get ticket cart by customer successful", ticketShoppingCartService.getTicketCartByCustomer()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getTicketCart(@RequestHeader("X-auth-token") String token, @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get ticket cart successful", ticketShoppingCartService.getTicketCart(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<ApiResponse> editTicket(@RequestHeader("X-auth-token") String token, @RequestBody TicketCartEditDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            ticketShoppingCartService.editTicketCart(dto);
            return ResponseEntity.ok(ApiResponse.success("Edit ticket successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
