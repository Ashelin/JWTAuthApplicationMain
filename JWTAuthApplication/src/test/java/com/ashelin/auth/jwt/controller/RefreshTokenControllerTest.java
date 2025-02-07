package com.ashelin.auth.jwt.controller;

import com.ashelin.auth.jwt.config.JwtService;
import com.ashelin.auth.jwt.dto.AuthenticationResponse;
import com.ashelin.auth.jwt.dto.RefreshTokenRequest;
import com.ashelin.auth.jwt.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RefreshTokenController.class)
class RefreshTokenControllerTest {

    @MockitoBean
    private RefreshTokenService refreshTokenService;
    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void refreshToken() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("refreshToken");
        AuthenticationResponse authenticationResponse = new AuthenticationResponse("token", "refreshToken");
        String refreshTokenRequestJson = objectMapper.writeValueAsString(refreshTokenRequest);

        when(refreshTokenService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(ResponseEntity.ok(authenticationResponse));

        mockMvc.perform(post("/api/v1/refresh-token")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenRequestJson))
                .andExpect(status().isOk());
    }
}