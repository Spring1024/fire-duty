package com.fireduty.task.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateTemplateRequest {

    private String name;

    private String description;

    /** fire_safety / equipment / evacuation / general */
    private String category;

    private String createdBy;

    private List<ItemDef> items;

    @Data
    public static class ItemDef {
        private String name;
        private String description;
        private String type;
        private Boolean required;
        private Integer sortOrder;
    }
}
