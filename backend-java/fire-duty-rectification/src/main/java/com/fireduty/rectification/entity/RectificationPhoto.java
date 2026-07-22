package com.fireduty.rectification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("rectification_photos")
public class RectificationPhoto {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long rectId;
    private String photoType;
    private String url;
    private LocalDateTime takenAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
