package com.fireduty.device.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DeviceDTO {

    private Long id;

    private String code;

    private String name;

    private String type;

    private Integer status;

    private String location;

    private Long gridId;

    private String gridPath;

    private String manufacturer;

    private LocalDate installDate;

    private LocalDate lastCheck;

    private LocalDate lastMaintenance;

    private String qrCode;

    private Double lat;

    private Double lng;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
