-- ============================================================================
-- 消防履职系统 — 用户模块 DDL (Flyway V2)
-- 表：users, roles, user_roles
-- 依赖：grids (V1)
-- ============================================================================

-- 1. roles — 角色表
CREATE TABLE IF NOT EXISTS roles (
    id              BIGSERIAL       PRIMARY KEY,
    code            VARCHAR(32)     NOT NULL UNIQUE,
    name            VARCHAR(64)     NOT NULL,
    description     TEXT,
    sort_order      INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- 2. users — 用户表（依赖 grids）
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

-- 3. user_roles — 用户角色关联表
CREATE TABLE IF NOT EXISTS user_roles (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id         BIGINT          NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_roles_uniq ON user_roles(user_id, role_id);
