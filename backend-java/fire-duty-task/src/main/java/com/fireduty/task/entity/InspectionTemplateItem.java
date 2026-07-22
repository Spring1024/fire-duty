package com.fireduty.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inspection_template_items")
public class InspectionTemplateItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long templateId;

    private String name;

    private String description;

    /** checkbox / text / select */
    private String type;

    private Boolean required;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
