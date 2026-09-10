package com.example.demo.model;

import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "github_events")
public class GitHubEvent {

    @Id
    private String id;

    // =================================================
    // WEBHOOK IDEMPOTENCY
    // =================================================
    //
    // GitHub sends a unique ID with every webhook:
    //
    // X-GitHub-Delivery
    //
    // We store it so the same webhook is not
    // processed more than once.
    //
    // unique = true ensures MongoDB does not allow
    // duplicate delivery IDs.
    // =================================================

    @Indexed(unique = true)
    private String deliveryId;


    // =================================================
    // GITHUB EVENT INFORMATION
    // =================================================

    private String eventType;

    private String action;

    private String repository;

    private String developer;


    // =================================================
    // PULL REQUEST INFORMATION
    // =================================================

    private Integer pullRequestNumber;

    private Boolean merged;


    // =================================================
    // PUSH / COMMIT INFORMATION
    // =================================================

    private String branch;

    private Integer commitCount;

    private String commitSha;

    private Instant commitTime;


    // =================================================
    // GITHUB ACTIONS / WORKFLOW INFORMATION
    // =================================================

    private String workflowName;

    private String workflowStatus;

    private String workflowConclusion;


    // =================================================
    // DEPLOYMENT INFORMATION
    // =================================================

    private Boolean deployment;

    private Instant deploymentTime;


    // =================================================
    // EVENT RECEIVED TIME
    // =================================================
    private Instant failureTime;
private Instant recoveredAt;
    private Instant receivedAt;
}