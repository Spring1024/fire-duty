package com.fireduty.user.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String name;
    private String username;
    private String password;
    /** 角色ID（关联 roles 表） */
    private Long roleId;
    private Long gridId;
    private String gridName;
    private String phone;
}
