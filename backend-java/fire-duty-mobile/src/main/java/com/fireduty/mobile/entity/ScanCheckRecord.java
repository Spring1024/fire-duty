package com.fireduty.mobile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("scan_check_records")
public class ScanCheckRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceCode;
    private String status;
    private String remark;
    private String imagePath;
    private Long userId;
    private String userName;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
