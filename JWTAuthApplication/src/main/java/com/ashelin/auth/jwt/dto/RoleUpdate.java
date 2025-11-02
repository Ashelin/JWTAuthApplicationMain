package com.ashelin.auth.jwt.dto;

import com.ashelin.auth.jwt.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdate {
    private UserRole userRole;
}

