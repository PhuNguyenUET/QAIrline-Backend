package com.qairline.qairline_backend.util;

import com.qairline.qairline_backend.client.model.Client;
import com.qairline.qairline_backend.client.model.CustomUserDetails;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class AuthenticationUtils {
    public static Client getCurrentUser() {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return customUserDetails.getClient();
    }
}

