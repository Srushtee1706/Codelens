package com.example.demo.repository;

import com.example.demo.model.GitHubEvent;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface GitHubEventRepository
        extends MongoRepository<GitHubEvent, String> {


    // =================================================
    // DEVELOPER ANALYTICS
    // =================================================

    long countByDeveloperAndEventTypeAndAction(
            String developer,
            String eventType,
            String action
    );


    long countByDeveloperAndEventTypeAndActionAndMergedTrue(
            String developer,
            String eventType,
            String action
    );


    long countByDeveloperAndEventTypeAndActionAndMergedFalse(
            String developer,
            String eventType,
            String action
    );


    @Aggregation(pipeline = {
            "{ $match: { developer: ?0, eventType: 'push' } }",
            "{ $group: { _id: null, totalCommits: { $sum: '$commitCount' } } }"
    })
    Integer sumCommitsByDeveloper(
            String developer
    );


    // =================================================
    // DORA METRICS
    // =================================================


    // -------------------------------------------------
    // Deployment Frequency
    // -------------------------------------------------

    long countByDeploymentTrueAndWorkflowConclusionAndDeploymentTimeBetween(
            String workflowConclusion,
            Instant start,
            Instant end
    );


    // =================================================
    // LEAD TIME FOR CHANGES
    // =================================================
    //
    // Lead Time =
    // Deployment Time - Commit Time
    //
    // Push and deployment events are correlated
    // using the common commit SHA.
    // =================================================


    // -------------------------------------------------
    // Find push event for a particular commit SHA
    // -------------------------------------------------

    GitHubEvent findFirstByCommitShaAndEventType(
            String commitSha,
            String eventType
    );


    // -------------------------------------------------
    // Find deployment event for a particular commit SHA
    // -------------------------------------------------

    GitHubEvent findFirstByCommitShaAndDeploymentTrue(
            String commitSha
    );


    // -------------------------------------------------
    // Find push events within a time range
    // -------------------------------------------------

    List<GitHubEvent> findByEventTypeAndCommitTimeBetween(
            String eventType,
            Instant start,
            Instant end
    );


    // =================================================
    // CHANGE FAILURE RATE
    // =================================================
    //
    // Change Failure Rate =
    //
    // Failed Deployments
    // ------------------ × 100
    // Total Deployments
    //
    // "success" → successful deployment
    // "failure" → failed deployment
    //
    // The deployment query above is used for both.
    // =================================================


    // =================================================
    // WEBHOOK IDEMPOTENCY
    // =================================================
    //
    // GitHub provides a unique delivery ID through:
    //
    // X-GitHub-Delivery
    //
    // We store this ID with every webhook event.
    //
    // Before processing a webhook, we check whether
    // this delivery ID already exists.
    //
    // If it exists:
    //     Do not process the event again.
    //
    // This prevents duplicate webhook processing.
    // =================================================

    boolean existsByDeliveryId(
            String deliveryId
    );


    // =================================================
    // MTTR
    // =================================================
    //
    // MTTR =
    //
    // Recovery Time - Failure Time
    //
    // We identify failed deployments and then find
    // the next successful deployment for the same
    // repository and branch.
    // =================================================


    // -------------------------------------------------
    // Find failed deployments within a time range
    // -------------------------------------------------

    List<GitHubEvent> findByDeploymentTrueAndWorkflowConclusionAndDeploymentTimeBetween(
            String workflowConclusion,
            Instant start,
            Instant end
    );


    // -------------------------------------------------
    // Find successful deployments after a failure
    // for the same repository and branch.
    //
    // Results are ordered from oldest to newest so
    // the first result represents the next recovery.
    // -------------------------------------------------

    List<GitHubEvent> findByRepositoryAndBranchAndDeploymentTrueAndWorkflowConclusionAndDeploymentTimeAfterOrderByDeploymentTimeAsc(
            String repository,
            String branch,
            String workflowConclusion,
            Instant time
    );


}