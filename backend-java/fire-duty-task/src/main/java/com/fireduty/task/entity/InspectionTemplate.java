package com.fireduty.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inspection_templates")
public class InspectionTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    /** fire_safety / equipment / evacuation / general */
    private String category;

    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
