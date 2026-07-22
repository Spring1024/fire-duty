package com.fireduty.grid.controller;

import com.fireduty.common.annotation.RequirePermission;
import com.fireduty.common.response.Result;
import com.fireduty.grid.dto.GridTreeNode;
import com.fireduty.grid.entity.Grid;
import com.fireduty.grid.service.GridService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/grids")
@RequiredArgsConstructor
public class GridController {

    private final GridService gridService;

    @GetMapping
    @RequirePermission(resource = "devices", action = "read")
    public Result<List<Grid>> list() {
        return Result.success(gridService.list());
    }

    @GetMapping("/tree")
    @RequirePermission(resource = "devices", action = "read")
    public Result<List<GridTreeNode>> tree() {
        return Result.success(gridService.tree());
    }

    @GetMapping("/{id}")
    @RequirePermission(resource = "devices", action = "read")
    public Result<Grid> get(@PathVariable Long id) {
        return Result.success(gridService.get(id));
    }

    @PostMapping
    @RequirePermission(resource = "devices", action = "write")
    public Result<Grid> create(@RequestBody Grid grid) {
        return Result.created(gridService.create(grid));
    }

    @PutMapping("/{id}")
    @RequirePermission(resource = "devices", action = "write")
    public Result<Grid> update(@PathVariable Long id, @RequestBody Grid grid) {
        return Result.success(gridService.update(id, grid));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(resource = "devices", action = "delete")
    public Result<Void> delete(@PathVariable Long id) {
        gridService.delete(id);
        return Result.success();
    }
}
