package com.ashelin.auth.jwt.service;

import com.ashelin.auth.jwt.config.JwtService;
import com.ashelin.auth.jwt.dto.AuthenticationResponse;
import com.ashelin.auth.jwt.dto.RefreshTokenRequest;
import com.ashelin.auth.jwt.model.RefreshToken;
import com.ashelin.auth.jwt.model.User;
import com.ashelin.auth.jwt.repository.RefreshTokenRepository;
import com.ashelin.auth.jwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveUserRefreshToken() {
        User user = new User();
        String refreshToken = "refreshToken";
        String jti = "jti";

        refreshTokenService.saveUserRefreshToken(user, refreshToken, jti);

        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void revokeAllUserTokens() {
        User user = new User();
        user.setId(1L);
        RefreshToken token = new RefreshToken();
        token.setRevoked(false);
        token.setExpired(false);
        List<RefreshToken> tokens = Collections.singletonList(token);

        when(refreshTokenRepository.findAllByUserIdAndExpiredIsFalseAndRevokedIsFalse(anyLong())).thenReturn(tokens);

        refreshTokenService.revokeAllUserTokens(user);

        assertTrue(token.isRevoked());
        assertTrue(token.isExpired());
        verify(refreshTokenRepository, times(1)).saveAll(tokens);
    }

    @Test
    void refreshToken() {
        String refreshToken = "refreshToken";
        String email = "email";
        String jti = "jti";
        User user = new User();
        user.setEmail(email);
        RefreshToken storedToken = new RefreshToken();
        storedToken.setRevoked(false);
        storedToken.setExpired(false);

        when(jwtService.extractUsername(anyString())).thenReturn(email);
        when(jwtService.extractClaim(anyString(), any())).thenReturn(jti);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByJti(anyString())).thenReturn(Optional.of(storedToken));
        when(jwtService.generateToken(any(User.class))).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("newRefreshToken");

        ResponseEntity<AuthenticationResponse> response = refreshTokenService.refreshToken(new RefreshTokenRequest(refreshToken));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newAccessToken", response.getBody().getToken());
        assertEquals("newRefreshToken", response.getBody().getRefreshToken());
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void refreshToken_InvalidToken() {
        String refreshToken = "refreshToken";
        String email = "email";
        String jti = "jti";

        when(jwtService.extractUsername(anyString())).thenReturn(email);
        when(jwtService.extractClaim(anyString(), any())).thenReturn(jti);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
        when(refreshTokenRepository.findByJti(anyString())).thenReturn(Optional.empty());

        ResponseEntity<AuthenticationResponse> response = refreshTokenService.refreshToken(new RefreshTokenRequest(refreshToken));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void refreshToken_RevokedOrExpiredToken() {
        String refreshToken = "refreshToken";
        String email = "email";
        String jti = "jti";
        RefreshToken storedToken = new RefreshToken();
        storedToken.setRevoked(true);
        storedToken.setExpired(true);

        when(jwtService.extractUsername(anyString())).thenReturn(email);
        when(jwtService.extractClaim(anyString(), any())).thenReturn(jti);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
        when(refreshTokenRepository.findByJti(anyString())).thenReturn(Optional.of(storedToken));

        ResponseEntity<AuthenticationResponse> response = refreshTokenService.refreshToken(new RefreshTokenRequest(refreshToken));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}