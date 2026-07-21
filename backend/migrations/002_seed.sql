-- ============================================================
-- 消防履职系统 — 种子数据脚本
-- ============================================================

-- 1. 网格数据（大网格 → 中网格 → 小网格）
INSERT INTO grids (id, name, level, parent_id, path, leader, contact, phone, scope, device_count) VALUES
(1, '未来科技产业园', '大网格', NULL, '1', '张三', '张三', '138****0001', 'A/B/C栋及地下车库', 568);

INSERT INTO grids (id, name, level, parent_id, path, leader, contact, phone, scope, device_count) VALUES
(2, 'A栋', '中网格', 1, '1/2', '李四', '李四', '138****0002', 'A栋1-6层', 180),
(3, 'B栋', '中网格', 1, '1/3', '王五', '王五', '138****0003', 'B栋1-8层', 205),
(4, 'C栋', '中网格', 1, '1/4', '赵六', '赵六', '138****0004', 'C栋1-5层', 183);

INSERT INTO grids (id, name, level, parent_id, path, leader, contact, phone, scope, device_count) VALUES
(5, 'A栋-1层', '小网格', 2, '1/2/5', '李四', '李四', '138****0002', 'A栋1层', 32),
(6, 'A栋-2层', '小网格', 2, '1/2/6', '李四', '李四', '138****0002', 'A栋2层', 28),
(7, 'A栋-3层', '小网格', 2, '1/2/7', '钱七', '钱七', '138****0005', 'A栋3层', 35),
(8, 'A栋-4层', '小网格', 2, '1/2/8', '李四', '李四', '138****0002', 'A栋4层', 30),
(9, 'A栋-5层', '小网格', 2, '1/2/9', '李四', '李四', '138****0002', 'A栋5层', 25),
(10, 'A栋-6层', '小网格', 2, '1/2/10', '李四', '李四', '138****0002', 'A栋6层', 30);

-- 2. 用户数据
INSERT INTO users (id, username, password_hash, name, phone, role, grid_id, status) VALUES
(1, 'admin', '$2a$10$PLACEHOLDER_ADMIN_HASH', '管理员', '138****0000', '超级管理员', 1, '正常'),
(2, 'zhangsan', '$2a$10$PLACEHOLDER_HASH', '张三', '138****0001', '大网格负责人', 1, '正常'),
(3, 'lisi', '$2a$10$PLACEHOLDER_HASH', '李四', '138****0002', '中网格组长', 2, '正常'),
(4, 'wangwu', '$2a$10$PLACEHOLDER_HASH', '王五', '138****0003', '小网格检查员', 3, '停用'),
(5, 'zhaoliu', '$2a$10$PLACEHOLDER_HASH', '赵六', '138****0004', '维保单位', 4, '正常'),
(6, 'qianqi', '$2a$10$PLACEHOLDER_HASH', '钱七', '138****0005', '小网格检查员', 5, '正常');

SELECT setval('users_id_seq', 6);
SELECT setval('grids_id_seq', 10);

-- 3. 权限数据（RBAC）
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
('维保单位', 'rectifications', 'write');
