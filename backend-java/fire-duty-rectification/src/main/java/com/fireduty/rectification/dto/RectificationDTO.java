package com.fireduty.rectification.dto;

import lombok.Data;
import java.util.List;

@Data
public class RectificationDTO {
    private Long id;
    private String description;
    private String deviceCode;
    private String deviceName;
    private String level;
    private String levelType;
    private String foundTime;
    private String assignee;
    private Long assigneeId;
    private String status;
    private String statusType;
    private String deadline;
    private String createdAt;
    private String updatedAt;
    private List<TimelineDTO> timeline;
    private List<PhotoDTO> photos;
}
