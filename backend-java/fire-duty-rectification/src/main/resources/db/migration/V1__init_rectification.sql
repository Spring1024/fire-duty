-- ============================================================================
-- 消防履职系统 — 整改工单模块 DDL (Flyway V1)
-- 表：rectifications, rectification_timeline, rectification_photos
-- 依赖：inspection_tasks (V5), devices (V4), users (V2)
-- ============================================================================

-- 1. rectifications — 整改工单表
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

-- 2. rectification_timeline — 整改时间线表
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

-- 3. rectification_photos — 整改照片表
CREATE TABLE IF NOT EXISTS rectification_photos (
    id              BIGSERIAL       PRIMARY KEY,
    rect_id         BIGINT          NOT NULL REFERENCES rectifications(id) ON DELETE CASCADE,
    photo_type      VARCHAR(16)     NOT NULL CHECK (photo_type IN ('before','after')),
    url             VARCHAR(512)    NOT NULL,
    taken_at        TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rectification_photos_rect_id   ON rectification_photos(rect_id);
CREATE INDEX IF NOT EXISTS idx_rectification_photos_type      ON rectification_photos(rect_id, photo_type);
