-- ============================================================================
-- 消防履职系统 — 通用触发器函数 (Flyway V1)
-- 所有模块共享：为含 updated_at 列的表自动更新时间戳
-- ============================================================================

-- 1. updated_at 自动更新的触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. 为所有含 updated_at 列的表挂载自动更新触发器
DO $$
DECLARE
    tbl text;
    tables_with_updated_at text[] := ARRAY['grids','users','devices','inspection_templates','inspection_tasks','rectifications'];
BEGIN
    FOREACH tbl IN ARRAY tables_with_updated_at
    LOOP
        EXECUTE format(
            'CREATE TRIGGER IF NOT EXISTS trg_%s_updated_at
             BEFORE UPDATE ON %I
             FOR EACH ROW
             EXECUTE FUNCTION update_updated_at_column()',
            tbl, tbl
        );
    END LOOP;
END;
$$;

-- 3. 种子数据：网格结构
INSERT INTO grids (id, name, level, parent_id, path, leader, contact, phone, scope, device_count) VALUES
(1, '未来科技产业园', '大网格', NULL, '1', '张三', '张三', '138****0001', 'A/B/C栋及地下车库', 568)
ON CONFLICT (id) DO NOTHING;

INSERT INTO grids (id, name, level, parent_id, path, leader, contact, phone, scope, device_count) VALUES
(2, 'A栋', '中网格', 1, '1/2', '李四', '李四', '138****0002', 'A栋1-6层', 180),
(3, 'B栋', '中网格', 1, '1/3', '王五', '王五', '138****0003', 'B栋1-8层', 205),
(4, 'C栋', '中网格', 1, '1/4', '赵六', '赵六', '138****0004', 'C栋1-5层', 183)
ON CONFLICT (id) DO NOTHING;

INSERT INTO grids (id, name, level, parent_id, path, leader, contact, phone, scope, device_count) VALUES
(5, 'A栋-1层', '小网格', 2, '1/2/5', '李四', '李四', '138****0002', 'A栋1层', 32),
(6, 'A栋-2层', '小网格', 2, '1/2/6', '李四', '李四', '138****0002', 'A栋2层', 28),
(7, 'A栋-3层', '小网格', 2, '1/2/7', '钱七', '钱七', '138****0005', 'A栋3层', 35),
(8, 'A栋-4层', '小网格', 2, '1/2/8', '李四', '李四', '138****0002', 'A栋4层', 30),
(9, 'A栋-5层', '小网格', 2, '1/2/9', '李四', '李四', '138****0002', 'A栋5层', 25),
(10, 'A栋-6层', '小网格', 2, '1/2/10', '李四', '李四', '138****0002', 'A栋6层', 30)
ON CONFLICT (id) DO NOTHING;

-- 4. 种子数据：用户
INSERT INTO users (id, username, password_hash, name, phone, role, grid_id, status) VALUES
(1, 'admin', '$2a$10$PLACEHOLDER_ADMIN_HASH', '管理员', '138****0000', '超级管理员', 1, '正常'),
(2, 'zhangsan', '$2a$10$PLACEHOLDER_HASH', '张三', '138****0001', '大网格负责人', 1, '正常'),
(3, 'lisi', '$2a$10$PLACEHOLDER_HASH', '李四', '138****0002', '中网格组长', 2, '正常'),
(4, 'wangwu', '$2a$10$PLACEHOLDER_HASH', '王五', '138****0003', '小网格检查员', 3, '停用'),
(5, 'zhaoliu', '$2a$10$PLACEHOLDER_HASH', '赵六', '138****0004', '维保单位', 4, '正常'),
(6, 'qianqi', '$2a$10$PLACEHOLDER_HASH', '钱七', '138****0005', '小网格检查员', 5, '正常')
ON CONFLICT (id) DO NOTHING;

-- 5. 序列重置
SELECT setval('users_id_seq', GREATEST(COALESCE((SELECT MAX(id) FROM users), 0), 6));
SELECT setval('grids_id_seq', GREATEST(COALESCE((SELECT MAX(id) FROM grids), 0), 10));
