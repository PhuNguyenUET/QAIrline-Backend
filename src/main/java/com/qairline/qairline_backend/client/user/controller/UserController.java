package com.qairline.qairline_backend.client.user.controller;

import com.qairline.qairline_backend.client.admin.model.Admin;
import com.qairline.qairline_backend.client.user.dto.UserEditDTO;
import com.qairline.qairline_backend.client.user.model.User;
import com.qairline.qairline_backend.common.api.ApiResponse;
import com.qairline.qairline_backend.client.dto.ChangePasswordRequest;
import com.qairline.qairline_backend.client.dto.CreateNewPasswordRequest;
import com.qairline.qairline_backend.client.user.service.UserService;
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

@RestController
@RequiredArgsConstructor
@Tag(name = "user")
@RequestMapping("/api/customer/v1")
public class UserController {
    @Value("${api.token}")
    private String apiToken;

    private final UserService userService;

    @PutMapping("/change_password")
    public ResponseEntity<ApiResponse> changePassword(@RequestHeader("X-auth-token") String token,
                                                      @RequestBody ChangePasswordRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            userService.changePassword(request);
            return ResponseEntity.ok(ApiResponse.success("Change password success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/email/change")
    public ResponseEntity<ApiResponse> changeEmail(@RequestHeader("X-auth-token") String token,
                                                   @RequestBody String newEmail) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            userService.changeEmail(newEmail);
            return ResponseEntity.ok(ApiResponse.success("Change email success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/email/validate")
    public ResponseEntity<ApiResponse> validateEmail(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            userService.sendConfirmEmail();
            return ResponseEntity.ok(ApiResponse.success("Send validation code success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/current_user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = User.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getUser(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            User user = userService.getCurrentUser();
            return ResponseEntity.ok(ApiResponse.success("Get user success", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/email/token")
    public ResponseEntity<ApiResponse> checkEmailToken(@RequestHeader("X-auth-token") String authToken,
                                                       @RequestBody String token) {
        try {
            Assert.isTrue(apiToken.equals(authToken), "Invalid token");
            userService.confirmEmail(token);
            return ResponseEntity.ok(ApiResponse.success("Email validated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/forgot_password")
    public ResponseEntity<ApiResponse> sendForgetPasswordToEmail(@RequestHeader("X-auth-token") String token,
                                                                 @RequestBody String email) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            userService.sendResetPasswordEmail(email);
            return ResponseEntity.ok(ApiResponse.success("Send password reset code success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/create_new_password")
    public ResponseEntity<ApiResponse> createNewPassword(@RequestHeader("X-auth-token") String token,
                                                         @RequestBody CreateNewPasswordRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            userService.createNewPassword(request);
            return ResponseEntity.ok(ApiResponse.success("Send password reset code success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> editUser(@RequestHeader("X-auth-token") String token,
                                                         @RequestBody UserEditDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            userService.editUser(dto);
            return ResponseEntity.ok(ApiResponse.success("Edit user successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
