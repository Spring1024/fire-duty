package com.fireduty.rectification.dto;

import lombok.Data;

@Data
public class RectificationQuery {
    private int page = 1;
    private int pageSize = 20;
    private String tab; // pending, ongoing, review, closed
}
