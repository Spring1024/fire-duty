package com.fireduty.mobile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("watermark_photos")
public class WatermarkPhoto {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceCode;
    private String location;
    private String inspector;
    private String filePath;
    private Long userId;
    private LocalDateTime takenAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
