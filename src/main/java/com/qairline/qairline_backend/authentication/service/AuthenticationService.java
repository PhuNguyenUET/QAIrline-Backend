package com.qairline.qairline_backend.authentication.service;

import com.qairline.qairline_backend.authentication.dto.AuthenticationRequest;
import com.qairline.qairline_backend.authentication.dto.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletRequest httpServletRequest);

    String refreshToken(String refreshToken);
}
