package com.fireduty.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inspection_results")
public class InspectionResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long templateItemId;

    private String value;

    private String imageUrl;

    private String remark;

    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
