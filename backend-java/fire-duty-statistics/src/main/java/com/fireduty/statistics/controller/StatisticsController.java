package com.fireduty.statistics.controller;

import com.fireduty.common.annotation.RequirePermission;
import com.fireduty.common.response.Result;
import com.fireduty.statistics.dto.*;
import com.fireduty.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard-stats")
    @RequirePermission(resource = "statistics", action = "read")
    public Result<DashboardStats> dashboardStats() {
        return Result.success(statisticsService.getDashboardStats());
    }

    @GetMapping("/dashboard-alerts")
    @RequirePermission(resource = "statistics", action = "read")
    public Result<List<DashboardAlert>> dashboardAlerts() {
        return Result.success(statisticsService.getDashboardAlerts());
    }

    @GetMapping("/compliance")
    @RequirePermission(resource = "statistics", action = "read")
    public Result<ComplianceData> compliance(@RequestParam(value = "months", defaultValue = "6") Integer months) {
        return Result.success(statisticsService.getCompliance(months));
    }

    @GetMapping("/hazard-distribution")
    @RequirePermission(resource = "statistics", action = "read")
    public Result<List<HazardItem>> hazardDistribution() {
        return Result.success(statisticsService.getHazardDistribution());
    }

    @GetMapping("/summary")
    @RequirePermission(resource = "statistics", action = "read")
    public Result<SummaryData> summary() {
        return Result.success(statisticsService.getSummary());
    }

    @GetMapping("/export")
    @RequirePermission(resource = "statistics", action = "read")
    public Result<Map<String, Object>> export() {
        return Result.success(Map.of(
                "stats", statisticsService.getDashboardStats(),
                "alerts", statisticsService.getDashboardAlerts(),
                "compliance", statisticsService.getCompliance(6),
                "hazardDistribution", statisticsService.getHazardDistribution(),
                "summary", statisticsService.getSummary()
        ));
    }
}
