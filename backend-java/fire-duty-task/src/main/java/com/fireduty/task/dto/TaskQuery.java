package com.fireduty.task.dto;

import lombok.Data;

@Data
public class TaskQuery {

    private String keyword;

    /** draft / pending / in_progress / completed / cancelled */
    private String status;

    private String priority;

    private Long templateId;

    private String assignedTo;

    private Integer page = 1;

    private Integer pageSize = 10;
}
