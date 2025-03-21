package com.qairline.qairline_backend.authentication.controller.user;

import com.qairline.qairline_backend.authentication.dto.AuthenticationRequest;
import com.qairline.qairline_backend.authentication.dto.AuthenticationResponse;
import com.qairline.qairline_backend.authentication.service.user_impl.UserAuthenticationServiceImpl;
import com.qairline.qairline_backend.common.api.ApiResponse;
import com.qairline.qairline_backend.client.user.dto.UserRegisterDTO;
import com.qairline.qairline_backend.client.user.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "user_authentication")
@RequestMapping("/api/customer/v1")
public class UserAuthenticationController {

    @Value("${api.token}")
    private String apiToken;

    private final UserAuthenticationServiceImpl userAuthenticationService;
    private final UserService userService;

    @PostMapping("/authenticate")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = AuthenticationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> authenticate (@RequestHeader("X-auth-token") String token,
                                            @RequestBody AuthenticationRequest request, HttpServletRequest httpRequest) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Authentication successful", userAuthenticationService.authenticate(request, httpRequest)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register (@RequestHeader("X-auth-token") String token,
                                                 @RequestBody UserRegisterDTO request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            userService.register(request);
            return ResponseEntity.ok(ApiResponse.success("Registration success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/refresh_jwt")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = String.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> refreshToken (@RequestHeader("X-auth-token") String token,
                                                   @RequestBody String refresh) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            String jwtToken = userAuthenticationService.refreshToken(refresh);
            if (jwtToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token"));
            }
            return ResponseEntity.ok(ApiResponse.success(jwtToken));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}

