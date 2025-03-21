package com.qairline.qairline_backend.client.service;

import com.qairline.qairline_backend.client.dto.ChangePasswordRequest;
import com.qairline.qairline_backend.client.dto.CreateNewPasswordRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface ClientService extends UserDetailsService {
    void changePassword(ChangePasswordRequest changePasswordRequest);

    void sendResetPasswordEmail(String email);

    void createNewPassword(CreateNewPasswordRequest request);
}
