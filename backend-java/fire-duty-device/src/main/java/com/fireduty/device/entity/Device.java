package com.fireduty.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("devices")
public class Device {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String type;

    private Integer status;

    private String location;

    @TableField("grid_id")
    private Long gridId;

    @TableField("grid_path")
    private String gridPath;

    private String manufacturer;

    @TableField("install_date")
    private LocalDate installDate;

    @TableField("last_check")
    private LocalDate lastCheck;

    @TableField("last_maintenance")
    private LocalDate lastMaintenance;

    @TableField("qr_code")
    private String qrCode;

    private Double lat;

    private Double lng;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
