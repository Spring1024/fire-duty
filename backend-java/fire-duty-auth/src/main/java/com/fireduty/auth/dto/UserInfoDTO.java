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

    /** 角色名称列表（支持多角色） */
    private List<String> roles;

    private List<String> authorities;

    private Long gridId;

    private LocalDateTime lastLogin;

    /** 兼容旧接口：返回第一个角色 */
    public String getRole() {
        return (roles != null && !roles.isEmpty()) ? roles.get(0) : null;
    }
}
