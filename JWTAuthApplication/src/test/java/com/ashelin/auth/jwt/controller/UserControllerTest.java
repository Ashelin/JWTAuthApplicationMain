package com.ashelin.auth.jwt.controller;

import com.ashelin.auth.jwt.config.JwtService;
import com.ashelin.auth.jwt.dto.SearchUser;
import com.ashelin.auth.jwt.dto.UserRequest;
import com.ashelin.auth.jwt.dto.UserResponse;
import com.ashelin.auth.jwt.enums.UserRole;
import com.ashelin.auth.jwt.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Set;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createUser() throws Exception {
        UserRequest userRequest = new UserRequest(
                "Daniel",
                "Shelest",
                "daniel.shelest1@gmail.com",
                "Test123", UserRole.USER);
        String userRequestJson = objectMapper.writeValueAsString(userRequest);

        when(userService.createUser(any(UserRequest.class))).thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

        mockMvc.perform(post("/api/v1/user/create")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestJson))
                .andExpect(status().isCreated());

        verify(userService, times(1)).createUser(userRequest);
    }

    @Test
    @WithMockUser
    void searchUsers() throws Exception {
        SearchUser searchUser = new SearchUser(1L,
                "Daniel",
                "Shelest",
                "daniel.shelest1@gmail.com"
        );

        Set<UserResponse> userResponses = Collections.singleton(new UserResponse(1L,
                "Daniel",
                "Shelest",
                "daniel.shelest1@gmail.com",
                UserRole.USER,
                null,
                null
        ));

        when(userService.searchUsers(any(SearchUser.class))).thenReturn(ResponseEntity.ok(userResponses));

        mockMvc.perform(get("/api/v1/user/search")
                        .param("id", String.valueOf(searchUser.getId()))
                        .param("firstName", searchUser.getFirstName())
                        .param("lastName", searchUser.getLastName())
                        .param("email", searchUser.getEmail())
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        verify(userService, times(1)).searchUsers(any(SearchUser.class));
    }

    @Test
    @WithMockUser
    void updateUser() throws Exception {
        UserRequest userRequest = new UserRequest(
                "Daniel",
                "Shelest",
                "daniel.shelest1@gmail.com",
                "Test123",
                UserRole.USER
        );
        String userRequestJson = objectMapper.writeValueAsString(userRequest);

        when(userService.updateUser(anyLong(), any(UserRequest.class))).thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT).build());

        mockMvc.perform(patch("/api/v1/user/update/1")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestJson))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).updateUser(1L, userRequest);
    }

    @Test
    @WithMockUser
    void deleteUser() throws Exception {
        when(userService.deleteUser(anyLong())).thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT).build());

        mockMvc.perform(delete("/api/v1/user/delete/1")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }
}