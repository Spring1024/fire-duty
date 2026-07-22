package com.fireduty.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ComplianceData {
    private List<String> months;
    private List<Double> rates;
}
