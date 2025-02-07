package com.ashelin.auth.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchUser {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}
