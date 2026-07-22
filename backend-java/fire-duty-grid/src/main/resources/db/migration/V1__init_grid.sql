-- ============================================================================
-- 消防履职系统 — 网格模块 DDL (Flyway V1)
-- 表：grids, grid_devices
-- 依赖：无（基础表）
-- ============================================================================

-- 1. grids — 网格表
CREATE TABLE IF NOT EXISTS grids (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(128)    NOT NULL,
    level           VARCHAR(16)     NOT NULL CHECK (level IN ('大网格','中网格','小网格')),
    parent_id       BIGINT          REFERENCES grids(id) ON DELETE SET NULL,
    path            VARCHAR(256),
    leader          VARCHAR(64),
    contact         VARCHAR(64),
    phone           VARCHAR(20),
    scope           TEXT,
    device_count    INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_grids_parent_id ON grids(parent_id);
CREATE INDEX IF NOT EXISTS idx_grids_level      ON grids(level);
CREATE INDEX IF NOT EXISTS idx_grids_path       ON grids(path);

-- 2. grid_devices — 网格设备关联表
CREATE TABLE IF NOT EXISTS grid_devices (
    id              BIGSERIAL       PRIMARY KEY,
    grid_id         BIGINT          NOT NULL REFERENCES grids(id) ON DELETE CASCADE,
    device_id       BIGINT          NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_grid_devices_grid_id   ON grid_devices(grid_id);
CREATE INDEX IF NOT EXISTS idx_grid_devices_device_id ON grid_devices(device_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_grid_devices_uniq ON grid_devices(grid_id, device_id);
