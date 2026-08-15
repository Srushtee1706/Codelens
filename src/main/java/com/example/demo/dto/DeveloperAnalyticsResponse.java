package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeveloperAnalyticsResponse {

    private String developer;

    private long prsOpened;

    private long prsMerged;

    private long prsClosed;

    private long commits;

    
}