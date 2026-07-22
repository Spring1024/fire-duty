package com.fireduty.user.dto;

import lombok.Data;

@Data
public class UserQuery {
    private int page = 1;
    private int pageSize = 20;
    private String role;
    private String status;
    private String search;
}
