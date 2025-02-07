package com.ashelin.auth.jwt.controller;

import com.ashelin.auth.jwt.dto.AuthenticationRequest;
import com.ashelin.auth.jwt.dto.AuthenticationResponse;
import com.ashelin.auth.jwt.dto.RegisterRequest;
import com.ashelin.auth.jwt.service.AuthenticationService;
import com.ashelin.auth.jwt.config.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void register() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "firstName",
                "lastName",
                "email",
                "password"
        );
        AuthenticationResponse authenticationResponse = new AuthenticationResponse("token", "refreshToken");
        String registerRequestJson = objectMapper.writeValueAsString(registerRequest);

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(authenticationResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void authenticate() throws Exception {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("username", "password");
        AuthenticationResponse authenticationResponse = new AuthenticationResponse("token", "refreshToken");
        String authenticationRequestJson = objectMapper.writeValueAsString(authenticationRequest);

        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(authenticationResponse);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authenticationRequestJson))
                .andExpect(status().isOk());
    }
}