package com.fireduty.task.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InspectionResultDTO {

    private Long id;

    private Long taskId;

    private Long templateItemId;

    private String itemName;

    private String value;

    private String imageUrl;

    private String remark;

    private String createdBy;

    private LocalDateTime createdAt;
}
