package com.fireduty.user.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String username;
    private String password;
    private String role;
    private Long gridId;
    private String gridName;
    private String phone;
    private String status;
}
