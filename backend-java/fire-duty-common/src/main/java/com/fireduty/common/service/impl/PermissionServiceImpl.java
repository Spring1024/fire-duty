package com.fireduty.common.service.impl;

import com.fireduty.common.service.PermissionService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 权限校验服务实现。
 * 基于内存映射的 RBAC 权限矩阵，支持热刷新。
 *
 * 权限矩阵：
 * | 角色            | devices   | tasks     | rectifications | users   | statistics |
 * |-----------------|-----------|-----------|----------------|---------|------------|
 * | 超级管理员      | *         | *         | *              | *       | *          |
 * | 大网格负责人    | read/write| read/write| read/write     | read    | read       |
 * | 中网格组长      | read      | read/write| read/write     | —       | read       |
 * | 小网格检查员    | read      | read      | read/write     | —       | —          |
 * | 维保单位        | read      | —         | read/write     | —       | —          |
 */
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    /** role → set of "resource:action" */
    private final Map<String, Set<String>> permissionMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadDefaultPermissions();
        log.info("权限矩阵已加载，共 {} 个角色", permissionMap.size());
    }

    @Override
    public boolean hasPermission(String role, String resource, String action) {
        if (role == null || resource == null || action == null) {
            return false;
        }

        Set<String> perms = permissionMap.get(role);
        if (perms == null) {
            return false;
        }

        // 超级管理员拥有所有权限
        if (perms.contains("*")) {
            return true;
        }

        // 精确匹配 "resource:action"
        String key = resource + ":" + action;
        if (perms.contains(key)) {
            return true;
        }

        // 通配匹配 "resource:*"
        if (perms.contains(resource + ":*")) {
            return true;
        }

        return false;
    }

    @Override
    public Map<String, String[]> getRolePermissions() {
        Map<String, String[]> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, Set<String>> entry : permissionMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toArray(new String[0]));
        }
        return result;
    }

    @Override
    public void refresh() {
        loadDefaultPermissions();
        log.info("权限矩阵已刷新");
    }

    private void loadDefaultPermissions() {
        permissionMap.clear();

        // 超级管理员 — 所有权限
        permissionMap.put("超级管理员", Set.of("*"));

        // 大网格负责人
        permissionMap.put("大网格负责人", Set.of(
                "devices:read", "devices:write",
                "tasks:read", "tasks:write",
                "rectifications:read", "rectifications:write",
                "users:read",
                "statistics:read"
        ));

        // 中网格组长
        permissionMap.put("中网格组长", Set.of(
                "devices:read",
                "tasks:read", "tasks:write",
                "rectifications:read", "rectifications:write",
                "statistics:read"
        ));

        // 小网格检查员
        permissionMap.put("小网格检查员", Set.of(
                "devices:read",
                "tasks:read",
                "rectifications:read", "rectifications:write"
        ));

        // 维保单位
        permissionMap.put("维保单位", Set.of(
                "devices:read",
                "rectifications:read", "rectifications:write"
        ));
    }
}
