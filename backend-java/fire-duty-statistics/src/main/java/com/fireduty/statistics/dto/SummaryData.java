package com.fireduty.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SummaryData {
    private double overallComplianceRate;
    private String topHazardType;
    private double topHazardRate;
}
