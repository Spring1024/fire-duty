package com.fireduty.device.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DeviceImportDTO {
    private String code;
    private String name;
    private String type;
    private String status;
    private String location;
    private String manufacturer;
    private LocalDate installDate;
}
