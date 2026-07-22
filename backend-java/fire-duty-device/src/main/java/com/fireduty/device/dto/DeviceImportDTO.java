package com.fireduty.device.dto;

import lombok.Data;

@Data
public class DeviceImportDTO {

    private String code;

    private String name;

    private String type;

    private Integer status;

    private String location;

    private Long gridId;

    private String manufacturer;

    private Double lat;

    private Double lng;
}
