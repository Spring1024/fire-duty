package com.fireduty.task.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmitTaskRequest {

    /** List of inspection result values keyed by template item id */
    private List<ItemResult> items;

    private String remark;

    private String completedBy;

    @Data
    public static class ItemResult {
        private Long templateItemId;
        private String value;
        private String imageUrl;
        private String remark;
    }
}
