package com.fireduty.grid.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fireduty.common.exception.BusinessException;
import com.fireduty.common.exception.ResourceNotFoundException;
import com.fireduty.grid.dto.GridTreeNode;
import com.fireduty.grid.entity.Grid;
import com.fireduty.grid.mapper.GridMapper;
import com.fireduty.grid.service.GridService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GridServiceImpl implements GridService {

    private final GridMapper gridMapper;

    @Override
    public List<Grid> list() {
        return gridMapper.selectList(
                new LambdaQueryWrapper<Grid>().orderByAsc(Grid::getId));
    }

    @Override
    public List<GridTreeNode> tree() {
        List<Grid> allGrids = gridMapper.selectList(
                new LambdaQueryWrapper<Grid>().orderByAsc(Grid::getId));

        // Build children map
        Map<Long, List<Grid>> childrenMap = allGrids.stream()
                .collect(Collectors.groupingBy(
                        g -> g.getParentId() != null ? g.getParentId() : 0L));

        // Sort children by ID
        childrenMap.values().forEach(list -> list.sort(Comparator.comparing(Grid::getId)));

        return buildTree(0L, childrenMap);
    }

    private List<GridTreeNode> buildTree(Long parentId, Map<Long, List<Grid>> childrenMap) {
        List<Grid> children = childrenMap.get(parentId);
        if (children == null) return List.of();

        List<GridTreeNode> nodes = new ArrayList<>();
        for (Grid g : children) {
            GridTreeNode node = new GridTreeNode();
            node.setId(g.getId());
            node.setName(g.getName());
            node.setLevel(g.getLevel());
            node.setParentId(g.getParentId());
            node.setPath(g.getPath());
            node.setLeader(g.getLeader());
            node.setCount(g.getDeviceCount() != null ? g.getDeviceCount() : 0);
            node.setContact(g.getContact());
            node.setPhone(g.getPhone());
            node.setScope(g.getScope());
            node.setChildren(buildTree(g.getId(), childrenMap));
            nodes.add(node);
        }
        return nodes;
    }

    @Override
    public Grid get(Long id) {
        Grid grid = gridMapper.selectById(id);
        if (grid == null) throw new ResourceNotFoundException("网格不存在");
        return grid;
    }

    @Override
    @Transactional
    public Grid create(Grid grid) {
        if (grid.getParentId() != null && grid.getParentId() > 0) {
            Grid parent = gridMapper.selectById(grid.getParentId());
            if (parent == null) throw new BusinessException("父级网格不存在");
            grid.setPath(parent.getPath() + "/" + (gridMapper.selectCount(null) + 1));
            grid.setLevel(determineLevel(parent.getLevel()));
        } else {
            grid.setParentId(0L);
            grid.setPath("/" + (gridMapper.selectCount(null) + 1));
            grid.setLevel("大网格");
        }
        if (grid.getDeviceCount() == null) grid.setDeviceCount(0);
        gridMapper.insert(grid);
        log.info("Created grid: {}", grid.getName());
        return grid;
    }

    private String determineLevel(String parentLevel) {
        return switch (parentLevel) {
            case "大网格" -> "中网格";
            case "中网格" -> "小网格";
            default -> "中网格";
        };
    }

    @Override
    @Transactional
    public Grid update(Long id, Grid updated) {
        Grid existing = gridMapper.selectById(id);
        if (existing == null) throw new ResourceNotFoundException("网格不存在");

        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getLeader() != null) existing.setLeader(updated.getLeader());
        if (updated.getContact() != null) existing.setContact(updated.getContact());
        if (updated.getPhone() != null) existing.setPhone(updated.getPhone());
        if (updated.getScope() != null) existing.setScope(updated.getScope());
        if (updated.getDeviceCount() != null) existing.setDeviceCount(updated.getDeviceCount());

        gridMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Grid grid = gridMapper.selectById(id);
        if (grid == null) throw new ResourceNotFoundException("网格不存在");

        // Check for children
        Long childCount = gridMapper.selectCount(
                new LambdaQueryWrapper<Grid>().eq(Grid::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("该网格下存在子网格，无法删除");
        }
        gridMapper.deleteById(id);
    }
}
