package com.example.demo.model;

import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "github_events")
public class GitHubEvent {

    @Id
    private String id;

    private String eventType;

    private String action;

    private String repository;

    private String developer;

    private Integer pullRequestNumber;

    private Boolean merged;

    private String branch;

    private Integer commitCount;

    private String workflowName;

    private String workflowStatus;

    private String workflowConclusion;

    private String commitSha;

    private Boolean deployment;

    private Instant receivedAt;
}