package com.fireduty.grid.dto;

import lombok.Data;
import java.util.List;

@Data
public class GridTreeNode {
    private Long id;
    private String name;
    private String level;
    private Long parentId;
    private String path;
    private String leader;
    private Integer count;
    private String contact;
    private String phone;
    private String scope;
    private List<GridTreeNode> children;
}
