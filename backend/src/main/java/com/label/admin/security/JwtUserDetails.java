package com.label.admin.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtUserDetails {

    private Long userId;
    private String username;
    private String roleCode;
}
