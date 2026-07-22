-- ============================================================================
-- 消防履职系统 — 移动端模块 DDL (Flyway V1)
-- 表：scan_check_records, watermark_photos, sync_queue
-- 依赖：users (V2), devices (V4)
-- ============================================================================

-- 1. scan_check_records — 扫码检查记录表
CREATE TABLE IF NOT EXISTS scan_check_records (
    id              BIGSERIAL       PRIMARY KEY,
    device_code     VARCHAR(64)     NOT NULL,
    status          VARCHAR(16)     NOT NULL CHECK (status IN ('正常','异常')),
    remark          TEXT,
    image_path      VARCHAR(512),
    user_id         BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    user_name       VARCHAR(64),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_scan_check_records_device_code ON scan_check_records(device_code);
CREATE INDEX IF NOT EXISTS idx_scan_check_records_user_id     ON scan_check_records(user_id);
CREATE INDEX IF NOT EXISTS idx_scan_check_records_created_at  ON scan_check_records(created_at);

-- 2. watermark_photos — 水印照片表
CREATE TABLE IF NOT EXISTS watermark_photos (
    id              BIGSERIAL       PRIMARY KEY,
    device_code     VARCHAR(64)     NOT NULL,
    location        VARCHAR(256),
    inspector       VARCHAR(64),
    file_path       VARCHAR(512)    NOT NULL,
    user_id         BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    taken_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_watermark_photos_device_code ON watermark_photos(device_code);
CREATE INDEX IF NOT EXISTS idx_watermark_photos_user_id     ON watermark_photos(user_id);
CREATE INDEX IF NOT EXISTS idx_watermark_photos_taken_at    ON watermark_photos(taken_at);

-- 3. sync_queue — 离线同步队列表
CREATE TABLE IF NOT EXISTS sync_queue (
    id              BIGSERIAL       PRIMARY KEY,
    operation       VARCHAR(32)     NOT NULL CHECK (operation IN ('create_task','submit_result','upload_photo','scan_check')),
    entity_type     VARCHAR(32)     NOT NULL,
    entity_id       BIGINT,
    payload         JSONB           NOT NULL,
    status          VARCHAR(16)     NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','synced','failed')),
    retry_count     INTEGER         NOT NULL DEFAULT 0,
    last_error      TEXT,
    synced_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sync_queue_status        ON sync_queue(status);
CREATE INDEX IF NOT EXISTS idx_sync_queue_entity        ON sync_queue(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_sync_queue_created_at    ON sync_queue(created_at);
