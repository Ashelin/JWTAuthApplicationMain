package com.ashelin.auth.jwt.service;

import com.ashelin.auth.jwt.dto.SearchUser;
import com.ashelin.auth.jwt.dto.UserRequest;
import com.ashelin.auth.jwt.dto.UserResponse;
import com.ashelin.auth.jwt.enums.UserRole;
import com.ashelin.auth.jwt.exception.UserNotFoundException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser() {
        UserRequest userRequest = new UserRequest(
                "Daniel",
                "Shelest",
                "daniel.shelest1@gmail.com",
                "Test123",
                UserRole.USER
        );
        User user = User.builder()
                .id(1L)
                .firstName("Daniel")
                .lastName("Shelest")
                .email("daniel.shelest1@gmail.com")
                .password("encodedPassword")
                .userRole(UserRole.USER)
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<Void> response = userService.createUser(userRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void searchUsers() {
        SearchUser searchUser = new SearchUser(1L,
                "Daniel",
                "Shelest",
                "daniel.shelest1@gmail.com"
        );
        User user = User.builder()
                .id(1L)
                .firstName("Daniel")
                .lastName("Shelest")
                .email("daniel.shelest1@gmail.com")
                .userRole(UserRole.USER)
                .build();
        Set<User> users = Collections.singleton(user);

        when(userRepository.search(anyString(), anyString(), anyLong(), anyString())).thenReturn(users);

        ResponseEntity<Set<UserResponse>> response = userService.searchUsers(searchUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        verify(userRepository, times(1)).search(anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    void updateUser() {
        UserRequest userRequest = new UserRequest(
                "Daniel_Updated",
                "Shelest",
                "daniel.shelest1@gmail.com",
                "Test123",
                UserRole.USER
        );
        User user = User.builder()
                .id(1L)
                .firstName("Daniel")
                .lastName("Shelest")
                .email("daniel.shelest1@gmail.com")
                .password("encodedPassword")
                .userRole(UserRole.USER)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        ResponseEntity<Void> response = userService.updateUser(1L, userRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(any(User.class));

        assertEquals("Daniel_Updated", user.getFirstName());
    }

    @Test
    void updateUser_UserNotFound() {
        UserRequest userRequest = new UserRequest(
                "Daniel",
                "Shelest",
                "daniel.shelest1@gmail.com",
                "Test123",
                UserRole.USER
        );

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, userRequest));
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void deleteUser() {
        when(userRepository.existsById(anyLong())).thenReturn(true);

        ResponseEntity<Void> response = userService.deleteUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(refreshTokenRepository, times(1)).deleteByUserId(anyLong());
        verify(userRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void deleteUser_UserNotFound() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
        verify(refreshTokenRepository, times(0)).deleteByUserId(anyLong());
        verify(userRepository, times(0)).deleteById(anyLong());
    }
}