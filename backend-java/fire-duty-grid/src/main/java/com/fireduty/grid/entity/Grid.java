package com.fireduty.grid.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("grids")
public class Grid {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String level;
    private Long parentId;
    private String path;
    private String leader;
    private String contact;
    private String phone;
    private String scope;
    private Integer deviceCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
