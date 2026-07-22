package com.fireduty.rectification.dto;

import lombok.Data;

@Data
public class TimelineDTO {
    private Long id;
    private String action;
    private String operator;
    private String comment;
    private String timestamp;
}
