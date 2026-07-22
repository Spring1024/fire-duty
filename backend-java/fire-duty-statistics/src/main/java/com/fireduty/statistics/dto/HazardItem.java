package com.fireduty.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HazardItem {
    private String type;
    private double rate;
    private String color;
}
