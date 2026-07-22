package com.fireduty.rectification.dto;

import lombok.Data;

@Data
public class PhotoDTO {
    private Long id;
    private String type;
    private String url;
    private String takenAt;
}
