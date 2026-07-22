package com.fireduty.grid.controller;

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
    public Result<List<Grid>> list() {
        return Result.success(gridService.list());
    }

    @GetMapping("/tree")
    public Result<List<GridTreeNode>> tree() {
        return Result.success(gridService.tree());
    }

    @GetMapping("/{id}")
    public Result<Grid> get(@PathVariable Long id) {
        return Result.success(gridService.get(id));
    }

    @PostMapping
    public Result<Grid> create(@RequestBody Grid grid) {
        return Result.created(gridService.create(grid));
    }

    @PutMapping("/{id}")
    public Result<Grid> update(@PathVariable Long id, @RequestBody Grid grid) {
        return Result.success(gridService.update(id, grid));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        gridService.delete(id);
        return Result.success();
    }
}
