-- ============================================================================
-- 消防履职系统 — 巡检任务模块 DDL (Flyway V1)
-- 表：inspection_templates, inspection_template_items, inspection_tasks, inspection_results
-- 依赖：devices (V4), users (V2)
-- ============================================================================

-- 1. inspection_templates — 巡检模板表
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

-- 2. inspection_template_items — 模板检查项表
CREATE TABLE IF NOT EXISTS inspection_template_items (
    id              BIGSERIAL       PRIMARY KEY,
    template_id     BIGINT          NOT NULL REFERENCES inspection_templates(id) ON DELETE CASCADE,
    name            VARCHAR(128)    NOT NULL,
    sort_order      INTEGER         NOT NULL DEFAULT 0,
    result_type     VARCHAR(16)     NOT NULL DEFAULT 'boolean' CHECK (result_type IN ('boolean','enum','text')),
    options         JSONB,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_inspection_template_items_template_id ON inspection_template_items(template_id);
CREATE INDEX IF NOT EXISTS idx_inspection_template_items_sort_order  ON inspection_template_items(template_id, sort_order);

-- 3. inspection_tasks — 巡检任务表（task_results 的父表）
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

-- 4. inspection_results — 巡检结果明细表
CREATE TABLE IF NOT EXISTS inspection_results (
    id              BIGSERIAL       PRIMARY KEY,
    task_id         BIGINT          NOT NULL REFERENCES inspection_tasks(id) ON DELETE CASCADE,
    item_id         BIGINT          NOT NULL REFERENCES inspection_template_items(id) ON DELETE CASCADE,
    result          VARCHAR(32)     NOT NULL DEFAULT '正常' CHECK (result IN ('正常','异常','不适用')),
    remark          TEXT,
    image_urls      JSONB,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_inspection_results_task_id ON inspection_results(task_id);
CREATE INDEX IF NOT EXISTS idx_inspection_results_item_id ON inspection_results(item_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_inspection_results_task_item ON inspection_results(task_id, item_id);
