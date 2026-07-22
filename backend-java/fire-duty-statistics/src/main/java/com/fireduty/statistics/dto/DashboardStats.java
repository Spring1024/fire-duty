package com.fireduty.statistics.dto;

import lombok.Data;

@Data
public class DashboardStats {
    private int totalDevices = 568;
    private double onlineRate = 93.7;
    private int todayInspections = 45;
    private int pendingRectifications = 8;
    private double completionRate = 71.1;
    private int plannedTasks = 63;
    private int completedTasks = 45;
    private int overdueTasks = 18;
}
