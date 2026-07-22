-- ============================================================================
-- 消防履职系统 — 统计报表模块 DDL (Flyway V1)
-- 表：stats_daily_compliance, stats_hazard_ranking
-- 依赖：inspection_results (V5), rectifications (V6)
-- ============================================================================

-- 1. stats_daily_compliance — 每日合规率统计表
CREATE TABLE IF NOT EXISTS stats_daily_compliance (
    id              BIGSERIAL       PRIMARY KEY,
    stat_date       DATE            NOT NULL,
    total_tasks     INTEGER         NOT NULL DEFAULT 0,
    completed_tasks INTEGER         NOT NULL DEFAULT 0,
    overdue_tasks   INTEGER         NOT NULL DEFAULT 0,
    compliance_rate DECIMAL(5,2)    NOT NULL DEFAULT 0.00,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_stats_daily_compliance_date ON stats_daily_compliance(stat_date);
CREATE INDEX IF NOT EXISTS idx_stats_daily_compliance_rate        ON stats_daily_compliance(stat_date, compliance_rate);

-- 2. stats_hazard_ranking — 隐患分类排名表
CREATE TABLE IF NOT EXISTS stats_hazard_ranking (
    id              BIGSERIAL       PRIMARY KEY,
    stat_month      VARCHAR(7)      NOT NULL,  -- YYYY-MM
    hazard_type     VARCHAR(32)     NOT NULL,
    count           INTEGER         NOT NULL DEFAULT 0,
    percentage      DECIMAL(5,2)    NOT NULL DEFAULT 0.00,
    rank_order      INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_stats_hazard_ranking_month ON stats_hazard_ranking(stat_month);
CREATE INDEX IF NOT EXISTS idx_stats_hazard_ranking_type  ON stats_hazard_ranking(stat_month, hazard_type);
