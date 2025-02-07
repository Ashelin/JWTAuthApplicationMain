package com.ashelin.auth.jwt.service;

import com.ashelin.auth.jwt.config.JwtService;
import com.ashelin.auth.jwt.dto.AuthenticationRequest;
import com.ashelin.auth.jwt.dto.AuthenticationResponse;
import com.ashelin.auth.jwt.dto.RegisterRequest;
import com.ashelin.auth.jwt.enums.UserRole;
import com.ashelin.auth.jwt.model.User;
import com.ashelin.auth.jwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register() {
        RegisterRequest request = new RegisterRequest(
                "firstName",
                "lastName",
                "email",
                "password"
        );
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password("encodedPassword")
                .userRole(UserRole.USER)
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");
        when(jwtService.extractClaim(anyString(), any())).thenReturn("jti");

        AuthenticationResponse response = authenticationService.register(request);

        assertNotNull(response);
        assertEquals("accessToken", response.getToken());
        assertEquals("refreshToken", response.getRefreshToken());
        verify(userRepository, times(1)).save(any(User.class));
        verify(refreshTokenService, times(1)).saveUserRefreshToken(any(User.class), anyString(), anyString());
    }

    @Test
    void authenticate() {
        AuthenticationRequest request = new AuthenticationRequest("email", "password");
        User user = User.builder()
                .email(request.getEmail())
                .password("encodedPassword")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");
        when(jwtService.extractClaim(anyString(), any())).thenReturn("jti");

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("accessToken", response.getToken());
        assertEquals("refreshToken", response.getRefreshToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(refreshTokenService, times(1)).revokeAllUserTokens(any(User.class));
        verify(refreshTokenService, times(1)).saveUserRefreshToken(any(User.class), anyString(), anyString());
    }

    @Test
    void authenticate_UserNotFound() {
        AuthenticationRequest request = new AuthenticationRequest("email", "password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(request));
    }
}