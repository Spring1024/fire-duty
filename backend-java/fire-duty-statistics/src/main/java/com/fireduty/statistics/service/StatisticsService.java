package com.fireduty.statistics.service;

import com.fireduty.statistics.dto.*;

import java.util.List;

public interface StatisticsService {
    DashboardStats getDashboardStats();
    List<DashboardAlert> getDashboardAlerts();
    ComplianceData getCompliance(int months);
    List<HazardItem> getHazardDistribution();
    SummaryData getSummary();
}
