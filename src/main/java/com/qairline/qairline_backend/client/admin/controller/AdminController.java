package com.qairline.qairline_backend.client.admin.controller;

import com.qairline.qairline_backend.client.admin.model.Admin;
import com.qairline.qairline_backend.client.admin.service.AdminService;
import com.qairline.qairline_backend.client.dto.ChangePasswordRequest;
import com.qairline.qairline_backend.client.dto.CreateNewPasswordRequest;
import com.qairline.qairline_backend.client.admin.dto.AdminInviteDTO;
import com.qairline.qairline_backend.common.api.ApiResponse;
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
@Tag(name = "admin")
@RequestMapping("/api/admin/v1")
public class AdminController {
    @Value("${api.token}")
    private String apiToken;

    private final AdminService adminService;

    @PutMapping("/change_password")
    public ResponseEntity<ApiResponse> changePassword(@RequestHeader("X-auth-token") String token,
                                                      @RequestBody ChangePasswordRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            adminService.changePassword(request);
            return ResponseEntity.ok(ApiResponse.success("Change password success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/invite_admin")
    public ResponseEntity<ApiResponse> addAdmins(@RequestHeader("X-auth-token") String token,
                                                 @RequestBody AdminInviteDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            adminService.inviteAdmin(dto);
            return ResponseEntity.ok(ApiResponse.success("Invite admin successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/forgot_password")
    public ResponseEntity<ApiResponse> sendForgetPasswordToEmail(@RequestHeader("X-auth-token") String token,
                                                                 @RequestBody String email) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            adminService.sendResetPasswordEmail(email);
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
            adminService.createNewPassword(request);
            return ResponseEntity.ok(ApiResponse.success("Send password reset code success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/user_accounts/lock")
    public ResponseEntity<ApiResponse> lockUserAccount(@RequestHeader("X-auth-token") String token,
                                                        @RequestBody String username) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            adminService.lockUser(username);
            return ResponseEntity.ok(ApiResponse.success("Account locked"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/user_accounts/unlock")
    public ResponseEntity<ApiResponse> unlockUserAccount(@RequestHeader("X-auth-token") String token,
                                                          @RequestBody String username) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            adminService.unlockUser(username);
            return ResponseEntity.ok(ApiResponse.success("Account unlocked"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/admin_accounts/lock")
    public ResponseEntity<ApiResponse> lockAdminAccount(@RequestHeader("X-auth-token") String token,
                                                        @RequestBody String email) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            adminService.lockAdmin(email);
            return ResponseEntity.ok(ApiResponse.success("Account locked"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/admin_accounts/unlock")
    public ResponseEntity<ApiResponse> unlockAdminAccount(@RequestHeader("X-auth-token") String token,
                                                        @RequestBody String email) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            adminService.unlockAdmin(email);
            return ResponseEntity.ok(ApiResponse.success("Account unlocked"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/current_admin")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = Admin.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAdmin(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            Admin admin = adminService.getCurrentAdmin();
            return ResponseEntity.ok(ApiResponse.success("Get admin success", admin));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
