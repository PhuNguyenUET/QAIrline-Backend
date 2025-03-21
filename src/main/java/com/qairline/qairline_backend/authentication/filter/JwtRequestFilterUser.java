package com.qairline.qairline_backend.authentication.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qairline.qairline_backend.common.api.ApiResponse;
import com.qairline.qairline_backend.common.exception.AuthorizationException;
import com.qairline.qairline_backend.client.model.CustomUserDetails;
import com.qairline.qairline_backend.client.user.service.UserService;
import com.qairline.qairline_backend.util.JwtUtilsUser;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilterUser extends OncePerRequestFilter {
    private final UserService userService;
    private final ObjectMapper mapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(JwtConstant.JWT_HEADER);
        String requestURI = request.getRequestURI();

        if (header != null && header.startsWith(JwtConstant.JWT_TOKEN_PREFIX) && requestURI.contains("/customer/")) {
            String token = header.substring(JwtConstant.JWT_TOKEN_PREFIX.length());
            String username;

            try {
                username = JwtUtilsUser.extractJwtUsername(token);
            } catch (JwtException e) {
                AuthorizationException exception = new AuthorizationException(e.getMessage());
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(mapper.writeValueAsString(new ApiResponse(exception.getStatus().value(), exception.getMessage())));
                return;
            }

            CustomUserDetails customUserDetails;

            try {
                customUserDetails = (CustomUserDetails) userService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                AuthorizationException exception = new AuthorizationException("User not found");
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(mapper.writeValueAsString(new ApiResponse(exception.getStatus().value(), exception.getMessage())));
                return;
            }

            if (!customUserDetails.getClient().isActive()) {
                AuthorizationException exception = new AuthorizationException("The account is locked, please contact our admin.");
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(mapper.writeValueAsString(new ApiResponse(exception.getStatus().value(), exception.getMessage())));
                return;
            }

            if (JwtUtilsUser.validateJwtToken(token, customUserDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}


