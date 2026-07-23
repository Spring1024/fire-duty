package com.fireduty.user.dto;

import lombok.Data;

@Data
public class UserQuery {
    private int page = 1;
    private int pageSize = 20;
    /** 按角色ID筛选 */
    private Long roleId;
    private Integer status;
    private String search;
}
