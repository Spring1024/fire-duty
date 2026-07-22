package com.fireduty.task.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDTO {

    private Long id;

    private String title;

    private String description;

    private String status;

    private String priority;

    private Long templateId;

    private String templateName;

    private String assignedTo;

    private String location;

    private LocalDateTime scheduledDate;

    private LocalDateTime completedDate;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** Results filled when task is completed / submitted */
    private List<InspectionResultDTO> results;
}
