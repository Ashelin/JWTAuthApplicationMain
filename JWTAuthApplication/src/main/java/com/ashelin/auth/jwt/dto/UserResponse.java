package com.ashelin.auth.jwt.dto;

import com.ashelin.auth.jwt.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole userRole;
    private LocalDateTime creationTimestamp;
    private LocalDateTime modificationTimestamp;
}