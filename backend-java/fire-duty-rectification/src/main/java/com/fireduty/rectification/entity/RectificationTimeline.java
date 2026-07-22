package com.fireduty.rectification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("rectification_timeline")
public class RectificationTimeline {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long rectId;
    private String action;
    private Long operatorId;
    private String comment;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
