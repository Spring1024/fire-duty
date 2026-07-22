package com.fireduty.statistics.service.impl;

import com.fireduty.statistics.dto.*;
import com.fireduty.statistics.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Override
    public DashboardStats getDashboardStats() {
        return new DashboardStats();
    }

    @Override
    public List<DashboardAlert> getDashboardAlerts() {
        return List.of(
                new DashboardAlert("P0", "A区3号楼消防栓水压异常"),
                new DashboardAlert("P1", "B区2层烟感探测器离线"),
                new DashboardAlert("P2", "C区5号灭火器即将过期"),
                new DashboardAlert("P1", "D区1层安全通道堵塞"),
                new DashboardAlert("P2", "E区4号楼疏散指示故障")
        );
    }

    @Override
    public ComplianceData getCompliance(int months) {
        return new ComplianceData(
                List.of("5月", "6月", "7月", "8月", "9月"),
                List.of(60.0, 70.0, 85.0, 80.0, 88.0)
        );
    }

    @Override
    public List<HazardItem> getHazardDistribution() {
        return List.of(
                new HazardItem("灭火器", 35.0, "#f56c6c"),
                new HazardItem("消火栓", 25.0, "#e6a23c"),
                new HazardItem("疏散通道", 20.0, "#409eff"),
                new HazardItem("报警系统", 12.0, "#67c23a"),
                new HazardItem("其他", 8.0, "#909399")
        );
    }

    @Override
    public SummaryData getSummary() {
        return new SummaryData(93.5, "灭火器", 35.0);
    }
}
