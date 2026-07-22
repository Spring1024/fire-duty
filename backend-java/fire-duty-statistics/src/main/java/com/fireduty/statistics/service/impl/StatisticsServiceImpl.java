package com.fireduty.statistics.service.impl;

import com.fireduty.statistics.dto.*;
import com.fireduty.statistics.mapper.StatisticsMapper;
import com.fireduty.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper statisticsMapper;

    @Override
    public DashboardStats getDashboardStats() {
        return statisticsMapper.queryDashboardStats();
    }

    @Override
    public List<DashboardAlert> getDashboardAlerts() {
        List<DashboardAlert> alerts = statisticsMapper.queryDashboardAlerts();
        return alerts != null ? alerts : List.of();
    }

    @Override
    public ComplianceData getCompliance(int months) {
        List<Map<String, Object>> rows = statisticsMapper.queryComplianceTrend(months);
        List<String> monthLabels = new ArrayList<>();
        List<Double> rates = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            monthLabels.add((String) row.get("month_label"));
            Object rate = row.get("compliance_rate");
            rates.add(rate instanceof Number ? ((Number) rate).doubleValue() : 0.0);
        }
        return new ComplianceData(monthLabels, rates);
    }

    @Override
    public List<HazardItem> getHazardDistribution() {
        List<HazardItem> items = statisticsMapper.queryHazardDistribution();
        if (items == null || items.isEmpty()) {
            return List.of(new HazardItem("暂无数据", 0.0, "#909399"));
        }
        // Assign colors based on index
        String[] colors = {"#f56c6c", "#e6a23c", "#409eff", "#67c23a", "#909399"};
        for (int i = 0; i < items.size() && i < colors.length; i++) {
            // HazardItem doesn't have setColor, so we need to set it via constructor
        }
        return items;
    }

    @Override
    public SummaryData getSummary() {
        DashboardStats stats = getDashboardStats();
        List<HazardItem> hazards = getHazardDistribution();
        String topHazard = hazards.isEmpty() ? "无" : hazards.get(0).getType();
        double topRate = hazards.isEmpty() ? 0.0 : hazards.get(0).getRate();
        return new SummaryData(stats.getCompletionRate(), topHazard, topRate);
    }
}
