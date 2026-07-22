package com.fireduty.task.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TemplateDTO {

    private Long id;

    private String name;

    private String description;

    private String category;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<TemplateItemDTO> items;

    @Data
    public static class TemplateItemDTO {
        private Long id;
        private String name;
        private String description;
        private String type;
        private Boolean required;
        private Integer sortOrder;
    }
}
