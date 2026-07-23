package com.fireduty.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String passwordHash;
    private String name;
    private String phone;
    private Long gridId;
    private Integer status;
    private LocalDateTime lastLogin;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 角色名称（非数据库字段，关联查询填充） */
    @TableField(exist = false)
    private String roleName;
}
