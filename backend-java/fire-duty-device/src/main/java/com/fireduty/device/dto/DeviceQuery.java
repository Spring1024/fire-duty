package com.fireduty.device.dto;

import lombok.Data;

@Data
public class DeviceQuery {

    private Integer page = 1;

    private Integer pageSize = 20;

    private String type;

    private Integer status;

    private Long gridId;

    private String keyword;
}
