-- ============================================================================
-- 消防履职系统 — 设备模块 DDL (Flyway V1)
-- 表：devices, device_types
-- 依赖：grids (V1)
-- ============================================================================

-- 1. device_types — 设备类型字典表
CREATE TABLE IF NOT EXISTS device_types (
    id              BIGSERIAL       PRIMARY KEY,
    code            VARCHAR(32)     NOT NULL UNIQUE,
    name            VARCHAR(64)     NOT NULL,
    description     TEXT,
    icon            VARCHAR(128),
    sort_order      INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- 2. devices — 设备表（依赖 grids）
CREATE TABLE IF NOT EXISTS devices (
    id              BIGSERIAL       PRIMARY KEY,
    code            VARCHAR(64)     NOT NULL,
    name            VARCHAR(128)    NOT NULL,
    type            VARCHAR(32)     NOT NULL CHECK (type IN ('灭火器','消火栓','烟感','喷淋')),
    status          VARCHAR(16)     NOT NULL DEFAULT '正常' CHECK (status IN ('正常','故障','维护中')),
    location        VARCHAR(256),
    grid_id         BIGINT          REFERENCES grids(id) ON DELETE SET NULL,
    grid_path       VARCHAR(256),
    manufacturer    VARCHAR(128),
    install_date    DATE,
    last_check      DATE,
    last_maintenance DATE,
    qr_code         VARCHAR(256),
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
