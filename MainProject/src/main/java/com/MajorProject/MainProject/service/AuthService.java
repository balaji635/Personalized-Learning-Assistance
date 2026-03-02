package com.MajorProject.MainProject.service;

import com.MajorProject.MainProject.dto.AuthResponse;
import com.MajorProject.MainProject.dto.LoginRequest;
import com.MajorProject.MainProject.dto.RegisterRequest;
import com.MajorProject.MainProject.model.User;
import com.MajorProject.MainProject.repositry.UserRepository;
import com.MajorProject.MainProject.config.JwtConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;

    // ----------------------------------------
    // REGISTER
    // ----------------------------------------
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", request.getEmail());

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtConfig.generateAccessToken(userDetails);
        String refreshToken = jwtConfig.generateRefreshToken(userDetails);

        // Set tokens in HttpOnly cookies
        setTokenCookies(response, accessToken, refreshToken);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ----------------------------------------
    // LOGIN
    // ----------------------------------------
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtConfig.generateAccessToken(userDetails);
        String refreshToken = jwtConfig.generateRefreshToken(userDetails);

        // Set tokens in HttpOnly cookies
        setTokenCookies(response, accessToken, refreshToken);

        log.info("User logged in: {}", request.getEmail());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ----------------------------------------
    // REFRESH TOKEN
    // ----------------------------------------
    public AuthResponse refreshToken(String refreshToken, HttpServletResponse response) {
        String email = jwtConfig.extractUsername(refreshToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        if (!jwtConfig.isTokenValid(refreshToken, userDetails) || !jwtConfig.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String newAccessToken = jwtConfig.generateAccessToken(userDetails);
        String newRefreshToken = jwtConfig.generateRefreshToken(userDetails);

        setTokenCookies(response, newAccessToken, newRefreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    // ----------------------------------------
    // LOGOUT — clear cookies
    // ----------------------------------------
    public void logout(HttpServletResponse response) {
        clearTokenCookies(response);
    }

    // ----------------------------------------
    // COOKIE HELPERS
    // ----------------------------------------
    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access token cookie — 24 hours
        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);   // JS cannot read this — XSS protection
        accessCookie.setSecure(false);    // Set true in production (HTTPS only)
        accessCookie.setPath("/");
        accessCookie.setMaxAge(86400);    // 24 hours in seconds
        response.addCookie(accessCookie);

        // Refresh token cookie — 7 days
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);   // Set true in production
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(604800);  // 7 days in seconds
        response.addCookie(refreshCookie);
    }

    private void clearTokenCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("access_token", "");
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refresh_token", "");
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);
    }

    // ----------------------------------------
    // HELPER
    // ----------------------------------------
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}