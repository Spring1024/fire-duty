package com.fireduty.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HazardItem {
    private String type;
    private double rate;
    private String color;
}
