package com.task9_springsecurity.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // Ambil semua roles user
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Cek apakah user punya ROLE_ADMIN
        boolean isAdmin = authorities.stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        // Redirect sesuai role
        if (isAdmin) {
            response.sendRedirect("/admin"); // langsung redirect, jangan panggil service
        } else {
            response.sendRedirect("/user"); // langsung redirect
        }
    }
}

