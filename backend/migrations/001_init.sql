-- ============================================================================
-- 消防履职系统 — 核心数据库 DDL
-- 版本: 001
-- 说明: 创建 11 张业务表，含主键、唯一约束、外键、索引
-- 注意: 本脚本不含 seed 数据，需单独执行初始化脚本
-- ============================================================================
-- 使用 IF NOT EXISTS 确保幂等执行
-- ============================================================================

BEGIN;

-- ============================================================================
-- 1. grids — 网格表（先建，因 users/devices 依赖它）
-- ============================================================================
CREATE TABLE IF NOT EXISTS grids (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(128)    NOT NULL,
    level           VARCHAR(16)     NOT NULL CHECK (level IN ('大网格','中网格','小网格')),
    parent_id       BIGINT          REFERENCES grids(id) ON DELETE SET NULL,
    path            VARCHAR(256),                          -- 物化路径 e.g. '1/2/5'
    leader          VARCHAR(64),
    contact         VARCHAR(64),
    phone           VARCHAR(20),
    scope           TEXT,                                   -- 管辖范围描述
    device_count    INTEGER         NOT NULL DEFAULT 0,     -- 设备数量（缓存）
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_grids_parent_id    ON grids(parent_id);
CREATE INDEX IF NOT EXISTS idx_grids_level         ON grids(level);
CREATE INDEX IF NOT EXISTS idx_grids_path          ON grids(path);

-- ============================================================================
-- 2. users — 用户表
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL       PRIMARY KEY,
    username        VARCHAR(64)     NOT NULL,
    password_hash   VARCHAR(256)    NOT NULL,
    name            VARCHAR(64)     NOT NULL,
    phone           VARCHAR(20),
    role            VARCHAR(32)     NOT NULL CHECK (role IN ('超级管理员','大网格负责人','中网格组长','小网格检查员','维保单位')),
    grid_id         BIGINT          REFERENCES grids(id) ON DELETE SET NULL,
    status          VARCHAR(16)     NOT NULL DEFAULT '正常' CHECK (status IN ('正常','停用')),
    last_login      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_grid_id         ON users(grid_id);
CREATE INDEX IF NOT EXISTS idx_users_role            ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_status          ON users(status);

-- ============================================================================
-- 3. devices — 设备表
-- ============================================================================
CREATE TABLE IF NOT EXISTS devices (
    id              BIGSERIAL       PRIMARY KEY,
    code            VARCHAR(64)     NOT NULL,
    name            VARCHAR(128)    NOT NULL,
    type            VARCHAR(32)     NOT NULL CHECK (type IN ('灭火器','消火栓','烟感','喷淋')),
    status          VARCHAR(16)     NOT NULL DEFAULT '正常' CHECK (status IN ('正常','故障','维护中')),
    location        VARCHAR(256),
    grid_id         BIGINT          REFERENCES grids(id) ON DELETE SET NULL,
    grid_path       VARCHAR(256),                          -- 物化路径如 'A栋/3层'
    manufacturer    VARCHAR(128),
    install_date    DATE,
    last_check      DATE,
    last_maintenance DATE,
    qr_code         VARCHAR(256),                           -- 二维码图片URL
    lat             DECIMAL(10,7),
    lng             DECIMAL(10,7),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_devices_code        ON devices(code);
CREATE INDEX IF NOT EXISTS idx_devices_grid_id            ON devices(grid_id);
CREATE INDEX IF NOT EXISTS idx_devices_type               ON devices(type);
CREATE INDEX IF NOT EXISTS idx_devices_status             ON devices(status);
CREATE INDEX IF NOT EXISTS idx_devices_grid_path          ON devices(grid_path);

-- ============================================================================
-- 4. inspection_templates — 巡检模板表
-- ============================================================================
CREATE TABLE IF NOT EXISTS inspection_templates (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(128)    NOT NULL,
    device_type     VARCHAR(32)     NOT NULL CHECK (device_type IN ('灭火器','消火栓','烟感','喷淋')),
    item_count      INTEGER         NOT NULL DEFAULT 0,
    cycle           VARCHAR(16)     NOT NULL CHECK (cycle IN ('每月','每季度','每年')),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_inspection_templates_device_type ON inspection_templates(device_type);
CREATE INDEX IF NOT EXISTS idx_inspection_templates_cycle       ON inspection_templates(cycle);

-- ============================================================================
-- 5. inspection_template_items — 模板检查项表
-- ============================================================================
CREATE TABLE IF NOT EXISTS inspection_template_items (
    id              BIGSERIAL       PRIMARY KEY,
    template_id     BIGINT          NOT NULL REFERENCES inspection_templates(id) ON DELETE CASCADE,
    name            VARCHAR(128)    NOT NULL,
    sort_order      INTEGER         NOT NULL DEFAULT 0,
    result_type     VARCHAR(16)     NOT NULL DEFAULT 'boolean' CHECK (result_type IN ('boolean','enum','text')),
    options         JSONB,                                   -- 枚举选项数组
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_inspection_template_items_template_id ON inspection_template_items(template_id);
CREATE INDEX IF NOT EXISTS idx_inspection_template_items_sort_order  ON inspection_template_items(template_id, sort_order);

-- ============================================================================
-- 6. inspection_tasks — 巡检任务表
-- ============================================================================
CREATE TABLE IF NOT EXISTS inspection_tasks (
    id              BIGSERIAL       PRIMARY KEY,
    device_id       BIGINT          NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    template_id     BIGINT          REFERENCES inspection_templates(id) ON DELETE SET NULL,
    assignee_id     BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    deadline        TIMESTAMPTZ,
    status          VARCHAR(16)     NOT NULL DEFAULT '待检查' CHECK (status IN ('待检查','已完成','已超时')),
    remark          TEXT,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_inspection_tasks_device_id     ON inspection_tasks(device_id);
CREATE INDEX IF NOT EXISTS idx_inspection_tasks_template_id   ON inspection_tasks(template_id);
CREATE INDEX IF NOT EXISTS idx_inspection_tasks_assignee_id   ON inspection_tasks(assignee_id);
CREATE INDEX IF NOT EXISTS idx_inspection_tasks_status        ON inspection_tasks(status);
CREATE INDEX IF NOT EXISTS idx_inspection_tasks_deadline      ON inspection_tasks(deadline);

-- ============================================================================
-- 7. inspection_results — 巡检结果明细表
-- ============================================================================
CREATE TABLE IF NOT EXISTS inspection_results (
    id              BIGSERIAL       PRIMARY KEY,
    task_id         BIGINT          NOT NULL REFERENCES inspection_tasks(id) ON DELETE CASCADE,
    item_id         BIGINT          NOT NULL REFERENCES inspection_template_items(id) ON DELETE CASCADE,
    result          VARCHAR(32)     NOT NULL DEFAULT '正常' CHECK (result IN ('正常','异常','不适用')),
    remark          TEXT,
    image_urls      JSONB,                                   -- 照片URL数组
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_inspection_results_task_id ON inspection_results(task_id);
CREATE INDEX IF NOT EXISTS idx_inspection_results_item_id ON inspection_results(item_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_inspection_results_task_item ON inspection_results(task_id, item_id);

-- ============================================================================
-- 8. rectifications — 整改工单表
-- ============================================================================
CREATE TABLE IF NOT EXISTS rectifications (
    id              BIGSERIAL       PRIMARY KEY,
    task_id         BIGINT          NOT NULL REFERENCES inspection_tasks(id) ON DELETE CASCADE,
    device_id       BIGINT          NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    description     TEXT            NOT NULL,
    level           VARCHAR(16)     NOT NULL CHECK (level IN ('紧急','一般')),
    status          VARCHAR(16)     NOT NULL DEFAULT '待派发' CHECK (status IN ('待派发','整改中','待复核','已闭环','已超时')),
    assignee_id     BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    deadline        TIMESTAMPTZ,
    found_time      TIMESTAMPTZ,
    closed_time     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rectifications_task_id       ON rectifications(task_id);
CREATE INDEX IF NOT EXISTS idx_rectifications_device_id     ON rectifications(device_id);
CREATE INDEX IF NOT EXISTS idx_rectifications_assignee_id   ON rectifications(assignee_id);
CREATE INDEX IF NOT EXISTS idx_rectifications_status        ON rectifications(status);
CREATE INDEX IF NOT EXISTS idx_rectifications_level         ON rectifications(level);

-- ============================================================================
-- 9. rectification_timeline — 整改时间线表
-- ============================================================================
CREATE TABLE IF NOT EXISTS rectification_timeline (
    id              BIGSERIAL       PRIMARY KEY,
    rect_id         BIGINT          NOT NULL REFERENCES rectifications(id) ON DELETE CASCADE,
    action          VARCHAR(32)     NOT NULL CHECK (action IN ('派发','整改提交','复核通过','复核驳回')),
    operator_id     BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    comment         TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rectification_timeline_rect_id      ON rectification_timeline(rect_id);
CREATE INDEX IF NOT EXISTS idx_rectification_timeline_operator_id  ON rectification_timeline(operator_id);
CREATE INDEX IF NOT EXISTS idx_rectification_timeline_created_at   ON rectification_timeline(rect_id, created_at);

-- ============================================================================
-- 10. rectification_photos — 整改照片表
-- ============================================================================
CREATE TABLE IF NOT EXISTS rectification_photos (
    id              BIGSERIAL       PRIMARY KEY,
    rect_id         BIGINT          NOT NULL REFERENCES rectifications(id) ON DELETE CASCADE,
    photo_type      VARCHAR(16)     NOT NULL CHECK (photo_type IN ('before','after')),
    url             VARCHAR(512)    NOT NULL,
    taken_at        TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rectification_photos_rect_id    ON rectification_photos(rect_id);
CREATE INDEX IF NOT EXISTS idx_rectification_photos_type      ON rectification_photos(rect_id, photo_type);

-- ============================================================================
-- 11. permissions — 权限表（RBAC）
-- ============================================================================
CREATE TABLE IF NOT EXISTS permissions (
    id              BIGSERIAL       PRIMARY KEY,
    role            VARCHAR(32)     NOT NULL,
    resource        VARCHAR(64)     NOT NULL,
    action          VARCHAR(32)     NOT NULL CHECK (action IN ('read','write','delete'))
);

CREATE INDEX IF NOT EXISTS idx_permissions_role     ON permissions(role);
CREATE INDEX IF NOT EXISTS idx_permissions_resource ON permissions(resource);
CREATE UNIQUE INDEX IF NOT EXISTS idx_permissions_uniq ON permissions(role, resource, action);

-- ============================================================================
-- 辅助: updated_at 自动更新的触发器函数
-- ============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 为所有含 updated_at 列的表挂载自动更新触发器
-- ============================================================================
DO $$
DECLARE
    tbl text;
    tables_with_updated_at text[] := ARRAY['grids','users','devices','inspection_templates','inspection_tasks','rectifications'];
BEGIN
    FOREACH tbl IN ARRAY tables_with_updated_at
    LOOP
        EXECUTE format(
            'CREATE TRIGGER trg_%s_updated_at
             BEFORE UPDATE ON %I
             FOR EACH ROW
             EXECUTE FUNCTION update_updated_at_column()',
            tbl, tbl
        );
    END LOOP;
END;
$$;

COMMIT;
