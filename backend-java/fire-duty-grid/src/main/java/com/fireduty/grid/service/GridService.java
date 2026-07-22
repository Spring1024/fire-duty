package com.fireduty.grid.service;

import com.fireduty.grid.dto.GridTreeNode;
import com.fireduty.grid.entity.Grid;

import java.util.List;

public interface GridService {
    List<Grid> list();
    List<GridTreeNode> tree();
    Grid get(Long id);
    Grid create(Grid grid);
    Grid update(Long id, Grid grid);
    void delete(Long id);
}
