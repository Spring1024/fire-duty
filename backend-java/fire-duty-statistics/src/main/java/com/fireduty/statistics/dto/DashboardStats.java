package com.fireduty.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private int totalDevices;
    private double onlineRate;
    private int todayInspections;
    private int pendingRectifications;
    private double completionRate;
    private int plannedTasks;
    private int completedTasks;
    private int overdueTasks;
}
