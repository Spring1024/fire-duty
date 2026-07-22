package com.fireduty.rectification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rectifications")
public class Rectification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long deviceId;
    private String description;
    private String level;
    private String status;
    private Long assigneeId;
    private LocalDateTime deadline;
    private LocalDateTime foundTime;
    private LocalDateTime closedTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
