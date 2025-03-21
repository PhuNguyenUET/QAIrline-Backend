package com.qairline.qairline_backend.authentication.service.user_impl;

import com.qairline.qairline_backend.authentication.dto.AuthenticationRequest;
import com.qairline.qairline_backend.authentication.dto.AuthenticationResponse;
import com.qairline.qairline_backend.authentication.filter.JwtConstant;
import com.qairline.qairline_backend.authentication.service.AuthenticationService;
import com.qairline.qairline_backend.common.exception.LockedUserException;
import com.qairline.qairline_backend.common.exception.UserNotFoundException;
import com.qairline.qairline_backend.common.exception.WrongPasswordException;
import com.qairline.qairline_backend.client.model.CustomUserDetails;
import com.qairline.qairline_backend.client.user.model.User;
import com.qairline.qairline_backend.client.user.service.UserService;
import com.qairline.qairline_backend.util.JwtUtilsUser;
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
public class UserAuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    private UserService userService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    @Qualifier("userAuthManager")
    private AuthenticationManager authenticationManager;

    private final LockedUserException lockedUserException = new LockedUserException("Too many wrong attempts. Account has already been locked.");
    private final WrongPasswordException wrongPasswordException = new WrongPasswordException("Wrong password");

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletRequest httpServletRequest) {
        final int FAILURE_LIMIT = 15;
        User user = userService.getUserByUsername(request.getUsername());
        if (user == null) {
            throw new UserNotFoundException(request.getUsername());
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException e) {
            user.setFailedAttempt(user.getFailedAttempt() + 1);
            if (user.getFailedAttempt() > FAILURE_LIMIT) {
                user.setFailedAttempt(0);
                user.setActive(false);
            }
            mongoTemplate.save(user);
            if (!user.isActive()) {
                throw lockedUserException;
            }
            throw wrongPasswordException;
        }
        if (!user.isActive()) {
            throw lockedUserException;
        }
        String jwt = JwtUtilsUser.generateJwtToken(user.getUsername());
        String refresh = JwtUtilsUser.generateRefreshToken(user.getUsername());
        return new AuthenticationResponse(jwt, refresh);
    }

    @Override
    public String refreshToken(String refreshToken) {
        if (refreshToken != null && refreshToken.startsWith(JwtConstant.JWT_TOKEN_PREFIX)) {
            String token = refreshToken.substring(JwtConstant.JWT_TOKEN_PREFIX.length());
            String username;

            try {
                username = JwtUtilsUser.extractRefreshUsername(token);
            } catch (JwtException e) {
                return null;
            }

            CustomUserDetails customUserDetails;

            try {
                customUserDetails = (CustomUserDetails) userService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                return null;
            }

            if (!customUserDetails.getClient().isActive()) {
                return null;
            }

            if (JwtUtilsUser.validateRefreshToken(token, customUserDetails)) {
                return JwtUtilsUser.generateJwtToken(username);
            }
        }

        return null;
    }
}
