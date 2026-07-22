package com.fireduty.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    private Long id;

    private String username;

    private String name;

    private String phone;

    private String role;

    private List<String> authorities;

    private Long gridId;

    private LocalDateTime lastLogin;
}
