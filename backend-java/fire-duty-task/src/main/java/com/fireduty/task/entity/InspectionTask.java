package com.fireduty.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inspection_tasks")
public class InspectionTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    /** draft / pending / in_progress / completed / cancelled */
    private String status;

    /** high / medium / low */
    private String priority;

    private Long templateId;

    private String assignedTo;

    private String location;

    private LocalDateTime scheduledDate;

    private LocalDateTime completedDate;

    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
