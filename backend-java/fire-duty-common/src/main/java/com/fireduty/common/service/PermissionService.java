package com.fireduty.common.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 权限校验服务。
 * 维护角色→权限的内存映射，支持热更新。
 * 超级管理员("*")拥有所有权限。
 */
public interface PermissionService {

    /**
     * 校验角色是否有指定资源的指定操作权限
     */
    boolean hasPermission(String role, String resource, String action);

    /**
     * 获取所有角色权限映射
     */
    Map<String, String[]> getRolePermissions();

    /**
     * 刷新权限缓存（从数据库重新加载）
     */
    void refresh();
}
