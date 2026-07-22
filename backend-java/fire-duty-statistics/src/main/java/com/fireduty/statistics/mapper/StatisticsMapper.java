package com.fireduty.statistics.mapper;

import com.fireduty.statistics.dto.DashboardAlert;
import com.fireduty.statistics.dto.DashboardStats;
import com.fireduty.statistics.dto.HazardItem;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 统计模块 Mapper：真实数据库查询。
 * 使用 @Select 注解直接编写原生 SQL。
 */
public interface StatisticsMapper {

    /**
     * 仪表盘统计：设备总数、在线率、今日巡检数、待整改数、任务完成率
     */
    @Select("""
            SELECT
                (SELECT COUNT(*) FROM devices) AS total_devices,
                ROUND((SELECT COUNT(*) FROM devices WHERE status = '正常')::DECIMAL
                    / GREATEST((SELECT COUNT(*) FROM devices), 1) * 100, 1) AS online_rate,
                (SELECT COUNT(*) FROM inspection_tasks
                    WHERE created_at >= CURRENT_DATE) AS today_inspections,
                (SELECT COUNT(*) FROM rectifications
                    WHERE status IN ('待派发','整改中')) AS pending_rectifications,
                ROUND(
                    (SELECT COUNT(*) FROM inspection_tasks WHERE status = '已完成')::DECIMAL
                    / GREATEST((SELECT COUNT(*) FROM inspection_tasks), 1) * 100, 1) AS completion_rate,
                (SELECT COUNT(*) FROM inspection_tasks WHERE status = '待检查') AS planned_tasks,
                (SELECT COUNT(*) FROM inspection_tasks WHERE status = '已完成') AS completed_tasks,
                (SELECT COUNT(*) FROM inspection_tasks WHERE status = '已超时') AS overdue_tasks
            """)
    DashboardStats queryDashboardStats();

    /**
     * 告警列表：未闭环且已超期的整改单
     */
    @Select("""
            SELECT
                CASE WHEN level = '紧急' THEN 'P0' ELSE 'P1' END AS severity,
                description
            FROM rectifications
            WHERE status NOT IN ('已闭环','已归档')
              AND deadline < NOW()
            ORDER BY deadline ASC
            LIMIT 20
            """)
    List<DashboardAlert> queryDashboardAlerts();

    /**
     * 合规率趋势：按月聚合
     */
    @Select("""
            SELECT
                TO_CHAR(stat_date, 'MM"月"') AS month_label,
                compliance_rate
            FROM stats_daily_compliance
            WHERE stat_date >= CURRENT_DATE - INTERVAL '#{months} months'
            ORDER BY stat_date ASC
            """)
    List<Map<String, Object>> queryComplianceTrend(int months);

    /**
     * 隐患分布：按整改单的 device_type 分组统计
     */
    @Select("""
            SELECT
                COALESCE(d.type, '其他') AS hazard_type,
                COUNT(*) AS cnt,
                ROUND(COUNT(*)::DECIMAL / GREATEST((SELECT COUNT(*) FROM rectifications), 1) * 100, 1) AS percentage
            FROM rectifications r
            LEFT JOIN devices d ON r.device_id = d.id
            GROUP BY d.type
            ORDER BY cnt DESC
            """)
    List<HazardItem> queryHazardDistribution();
}
