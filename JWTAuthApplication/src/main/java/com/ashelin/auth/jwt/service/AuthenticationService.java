package com.ashelin.auth.jwt.service;

import com.ashelin.auth.jwt.config.JwtService;
import com.ashelin.auth.jwt.dto.AuthenticationRequest;
import com.ashelin.auth.jwt.dto.AuthenticationResponse;
import com.ashelin.auth.jwt.dto.RegisterRequest;
import com.ashelin.auth.jwt.enums.UserRole;
import com.ashelin.auth.jwt.model.User;
import com.ashelin.auth.jwt.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " is already registered");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userRole(UserRole.USER)
                .build();

        userRepository.save(user);

        return getAuthenticationResponse(user);
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(request.getEmail()));

        return getAuthenticationResponse(user);
    }

    private AuthenticationResponse getAuthenticationResponse(User user) {
        refreshTokenService.revokeAllUserTokens(user);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("authorities", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        extraClaims.put("userRole", user.getUserRole().name());

        String accessToken = jwtService.generateToken(extraClaims, user);
        String refreshToken = jwtService.generateRefreshToken(user);
        String jti = jwtService.extractClaim(refreshToken, claims -> claims.get("jti", String.class));

        refreshTokenService.saveUserRefreshToken(user, refreshToken, jti);

        return AuthenticationResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}