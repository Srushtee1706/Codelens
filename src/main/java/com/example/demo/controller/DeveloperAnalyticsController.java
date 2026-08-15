package com.example.demo.controller;

import com.example.demo.dto.DeveloperAnalyticsResponse;
import com.example.demo.service.GitHubEventService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class DeveloperAnalyticsController {

    private final GitHubEventService eventService;

    public DeveloperAnalyticsController(
            GitHubEventService eventService
    ) {
        this.eventService = eventService;
    }

    @GetMapping("/developer/{developer}")
    public DeveloperAnalyticsResponse
    getDeveloperAnalytics(
            @PathVariable String developer
    ) {

        return eventService.getDeveloperAnalytics(
                developer
        );
    }
}