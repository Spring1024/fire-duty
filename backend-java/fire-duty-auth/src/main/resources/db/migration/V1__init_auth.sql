-- ============================================================================
-- 消防履职系统 — 认证权限模块 DDL (Flyway V1)
-- 表：permissions
-- 依赖：users (V2)
-- ============================================================================

-- 1. permissions — 权限表（RBAC）
CREATE TABLE IF NOT EXISTS permissions (
    id              BIGSERIAL       PRIMARY KEY,
    role            VARCHAR(32)     NOT NULL,
    resource        VARCHAR(64)     NOT NULL,
    action          VARCHAR(32)     NOT NULL CHECK (action IN ('read','write','delete','*'))
);

CREATE INDEX IF NOT EXISTS idx_permissions_role     ON permissions(role);
CREATE INDEX IF NOT EXISTS idx_permissions_resource ON permissions(resource);
CREATE UNIQUE INDEX IF NOT EXISTS idx_permissions_uniq ON permissions(role, resource, action);

-- 2. 种子数据：初始角色权限
INSERT INTO permissions (role, resource, action) VALUES
('超级管理员', '*', '*'),
('大网格负责人', 'devices', 'read'),
('大网格负责人', 'devices', 'write'),
('大网格负责人', 'tasks', '*'),
('大网格负责人', 'rectifications', '*'),
('大网格负责人', 'statistics', 'read'),
('大网格负责人', 'users', 'read'),
('中网格组长', 'devices', 'read'),
('中网格组长', 'tasks', '*'),
('中网格组长', 'rectifications', 'read'),
('中网格组长', 'rectifications', 'write'),
('小网格检查员', 'devices', 'read'),
('小网格检查员', 'tasks', 'read'),
('小网格检查员', 'tasks', 'write'),
('小网格检查员', 'rectifications', 'read'),
('维保单位', 'devices', 'read'),
('维保单位', 'rectifications', 'read'),
('维保单位', 'rectifications', 'write')
ON CONFLICT (role, resource, action) DO NOTHING;
