package com.example.demo.controller;

import com.example.demo.dto.DoraMetricsResponse;
import com.example.demo.service.GitHubEventService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/analytics/dora")
public class DoraMetricsController {

    private final GitHubEventService eventService;

    public DoraMetricsController(GitHubEventService eventService) {
        this.eventService = eventService;
    }


    @GetMapping
    public DoraMetricsResponse getDoraMetrics(
            @RequestParam Instant start,
            @RequestParam Instant end
    ) {

        return eventService.getDoraMetrics(
                start,
                end
        );
    }
}