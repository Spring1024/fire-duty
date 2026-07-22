package com.fireduty.task.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTaskRequest {

    private String title;

    private String description;

    /** draft / pending */
    private String status;

    /** high / medium / low */
    private String priority;

    private Long templateId;

    private String assignedTo;

    private String location;

    private LocalDateTime scheduledDate;

    private String createdBy;
}
