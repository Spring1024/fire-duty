package com.fireduty.mobile.dto;

import lombok.Data;

@Data
public class ScanCheckRequest {
    private String deviceCode;
    private String status;
    private String remark;
    private String imageData;
}
