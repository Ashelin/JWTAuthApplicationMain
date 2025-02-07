package com.ashelin.auth.jwt.service;

import com.ashelin.auth.jwt.config.JwtService;
import com.ashelin.auth.jwt.dto.AuthenticationResponse;
import com.ashelin.auth.jwt.dto.RefreshTokenRequest;
import com.ashelin.auth.jwt.model.RefreshToken;
import com.ashelin.auth.jwt.model.User;
import com.ashelin.auth.jwt.repository.RefreshTokenRepository;
import com.ashelin.auth.jwt.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public void saveUserRefreshToken(User user, String refreshToken, String jti) {
        RefreshToken token = RefreshToken.builder()
                .token(refreshToken)
                .jti(jti)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .expired(false)
                .build();
        refreshTokenRepository.save(token);
    }

    public void revokeAllUserTokens(User user) {
        List<RefreshToken> validTokens = refreshTokenRepository.findAllByUserIdAndExpiredIsFalseAndRevokedIsFalse(user.getId());

        if (!validTokens.isEmpty()) {
            validTokens.forEach(token -> {
                token.setRevoked(true);
                token.setExpired(true);
            });
            refreshTokenRepository.saveAll(validTokens);
        }
    }

    @Transactional
    public ResponseEntity<AuthenticationResponse> refreshToken(RefreshTokenRequest request) {
        try {
            String email = jwtService.extractUsername(request.getRefreshToken());
            String jti = jwtService.extractClaim(request.getRefreshToken(), claims -> claims.get("jti", String.class));

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException(email));

            RefreshToken storedToken = refreshTokenRepository.findByJti(jti)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

            if (storedToken.isRevoked() || storedToken.isExpired()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);

            String newAccessToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);
            String newJti = jwtService.extractClaim(newRefreshToken, claims -> claims.get("jti", String.class));

            saveUserRefreshToken(user, newRefreshToken, newJti);

            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}