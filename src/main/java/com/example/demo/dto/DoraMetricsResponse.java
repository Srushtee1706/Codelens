package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoraMetricsResponse {

    private long deploymentFrequency;

    private double changeFailureRate;

    private Double leadTimeForChanges;

    private Double mttr;
}