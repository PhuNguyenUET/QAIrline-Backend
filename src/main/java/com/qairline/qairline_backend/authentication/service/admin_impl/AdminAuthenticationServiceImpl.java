package com.qairline.qairline_backend.authentication.service.admin_impl;

import com.qairline.qairline_backend.authentication.dto.AuthenticationRequest;
import com.qairline.qairline_backend.authentication.dto.AuthenticationResponse;
import com.qairline.qairline_backend.authentication.filter.JwtConstant;
import com.qairline.qairline_backend.authentication.service.AuthenticationService;
import com.qairline.qairline_backend.client.admin.model.Admin;
import com.qairline.qairline_backend.client.admin.service.AdminService;
import com.qairline.qairline_backend.client.model.CustomUserDetails;
import com.qairline.qairline_backend.common.exception.UserNotFoundException;
import com.qairline.qairline_backend.common.exception.WrongPasswordException;
import com.qairline.qairline_backend.util.JwtUtilsAdmin;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    private AdminService adminService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    @Qualifier("adminAuthManager")
    private AuthenticationManager authenticationManager;

    private final WrongPasswordException wrongPasswordException = new WrongPasswordException("Wrong password");

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletRequest httpServletRequest) {
        final int FAILURE_LIMIT = 15;
        Admin admin = adminService.getAdminByUsername(request.getUsername());
        if (admin == null) {
            throw new UserNotFoundException(request.getUsername());
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw wrongPasswordException;
        }
        String jwt = JwtUtilsAdmin.generateJwtToken(admin.getUsername());
        String refresh = JwtUtilsAdmin.generateRefreshToken(admin.getUsername());
        return new AuthenticationResponse(jwt, refresh);
    }

    @Override
    public String refreshToken(String refreshToken) {
        if (refreshToken != null && refreshToken.startsWith(JwtConstant.JWT_TOKEN_PREFIX)) {
            String token = refreshToken.substring(JwtConstant.JWT_TOKEN_PREFIX.length());
            String username;

            try {
                username = JwtUtilsAdmin.extractRefreshUsername(token);
            } catch (JwtException e) {
                return null;
            }

            CustomUserDetails customUserDetails;

            try {
                customUserDetails = (CustomUserDetails) adminService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                return null;
            }

            if (!customUserDetails.getClient().isActive()) {
                return null;
            }

            if (JwtUtilsAdmin.validateRefreshToken(token, customUserDetails)) {
                return JwtUtilsAdmin.generateJwtToken(username);
            }
        }

        return null;
    }
}
