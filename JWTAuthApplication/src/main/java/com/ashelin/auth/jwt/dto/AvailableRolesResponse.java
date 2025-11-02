package com.ashelin.auth.jwt.dto;

import com.ashelin.auth.jwt.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableRolesResponse {
    private List<UserRole> roles;
}

