package com.fireduty.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("password_hash")
    private String passwordHash;

    @TableField("name")
    private String name;

    @TableField("phone")
    private String phone;

    @TableField("role")
    private String role;

    @TableField("grid_id")
    private Long gridId;

    @TableField("status")
    private Integer status;

    @TableField("last_login")
    private LocalDateTime lastLogin;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
