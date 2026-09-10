package com.example.demo.service;

import com.example.demo.dto.DeveloperAnalyticsResponse;
import com.example.demo.dto.DoraMetricsResponse;
import com.example.demo.model.GitHubEvent;
import com.example.demo.repository.GitHubEventRepository;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class GitHubEventService {

    private final GitHubEventRepository repository;

    public GitHubEventService(GitHubEventRepository repository) {
        this.repository = repository;
    }


    // =================================================
    // EVENT STORAGE
    // =================================================

    public GitHubEvent saveEvent(GitHubEvent event) {
        return repository.save(event);
    }


    // =================================================
    // WEBHOOK IDEMPOTENCY
    // =================================================

    /*
     * GitHub sends a unique X-GitHub-Delivery ID
     * with every webhook request.
     *
     * We check MongoDB using this ID before processing
     * the webhook.
     *
     * If the ID already exists, the webhook has already
     * been processed and we can ignore it.
     */

    public boolean existsByDeliveryId(String deliveryId) {
        return repository.existsByDeliveryId(deliveryId);
    }


    // =================================================
    // DEVELOPER ANALYTICS
    // =================================================

    public long countPRsOpened(String developer) {

        return repository.countByDeveloperAndEventTypeAndAction(
                developer,
                "pull_request",
                "opened"
        );
    }


    public long countPRsMerged(String developer) {

        return repository.countByDeveloperAndEventTypeAndActionAndMergedTrue(
                developer,
                "pull_request",
                "closed"
        );
    }


    public long countPRsClosed(String developer) {

        return repository.countByDeveloperAndEventTypeAndActionAndMergedFalse(
                developer,
                "pull_request",
                "closed"
        );
    }


    public long countCommits(String developer) {

        Integer total =
                repository.sumCommitsByDeveloper(developer);

        return total != null ? total : 0;
    }


    // =================================================
    // COMPLETE DEVELOPER ANALYTICS
    // =================================================

    public DeveloperAnalyticsResponse getDeveloperAnalytics(
            String developer
    ) {

        long prsOpened =
                countPRsOpened(developer);

        long prsMerged =
                countPRsMerged(developer);

        long prsClosed =
                countPRsClosed(developer);

        long commits =
                countCommits(developer);


        return new DeveloperAnalyticsResponse(
                developer,
                prsOpened,
                prsMerged,
                prsClosed,
                commits
        );
    }


    // =================================================
    // DORA METRICS
    // =================================================

    // -------------------------------------------------
    // Deployment Frequency
    // -------------------------------------------------

    public long getDeploymentFrequency(
            Instant start,
            Instant end
    ) {

        return repository
                .countByDeploymentTrueAndWorkflowConclusionAndDeploymentTimeBetween(
                        "success",
                        start,
                        end
                );
    }


    // -------------------------------------------------
    // Successful Deployments
    // -------------------------------------------------

    public long getSuccessfulDeployments(
            Instant start,
            Instant end
    ) {

        return repository
                .countByDeploymentTrueAndWorkflowConclusionAndDeploymentTimeBetween(
                        "success",
                        start,
                        end
                );
    }


    // -------------------------------------------------
    // Failed Deployments
    // -------------------------------------------------

    public long getFailedDeployments(
            Instant start,
            Instant end
    ) {

        return repository
                .countByDeploymentTrueAndWorkflowConclusionAndDeploymentTimeBetween(
                        "failure",
                        start,
                        end
                );
    }


    // -------------------------------------------------
    // Change Failure Rate
    // -------------------------------------------------

    /*
     *
     * Change Failure Rate =
     *
     * Failed Deployments
     * ------------------------- × 100
     * Total Deployments
     *
     */

    public double getChangeFailureRate(
            Instant start,
            Instant end
    ) {

        long successfulDeployments =
                getSuccessfulDeployments(
                        start,
                        end
                );

        long failedDeployments =
                getFailedDeployments(
                        start,
                        end
                );


        long totalDeployments =
                successfulDeployments
                        + failedDeployments;


        // Avoid division by zero

        if (totalDeployments == 0) {
            return 0.0;
        }


        return (
                (double) failedDeployments
                        / totalDeployments
        ) * 100;
    }


    // =================================================
    // LEAD TIME FOR CHANGES
    // =================================================

    /*
     *
     * Lead Time =
     *
     * Deployment Time - Commit Time
     *
     * We correlate the push event and deployment event
     * using the commit SHA.
     *
     */

    public Double getLeadTimeForChange(
            String commitSha
    ) {

        // Find the push event for this commit

        GitHubEvent commitEvent =
                repository.findFirstByCommitShaAndEventType(
                        commitSha,
                        "push"
                );


        // Find the deployment associated with
        // the same commit SHA

        GitHubEvent deploymentEvent =
                repository.findFirstByCommitShaAndDeploymentTrue(
                        commitSha
                );


        // If either event is missing,
        // lead time cannot be calculated.

        if (commitEvent == null
                || deploymentEvent == null) {

            return null;
        }


        Instant commitTime =
                commitEvent.getCommitTime();

        Instant deploymentTime =
                deploymentEvent.getDeploymentTime();


        if (commitTime == null
                || deploymentTime == null) {

            return null;
        }


        // Calculate time difference

        Duration duration =
                Duration.between(
                        commitTime,
                        deploymentTime
                );


        // Return lead time in minutes

        return duration.toSeconds() / 60.0;
    }


    // -------------------------------------------------
    // Average Lead Time
    // -------------------------------------------------

    public Double getAverageLeadTime(
            Instant start,
            Instant end
    ) {

        List<GitHubEvent> pushEvents =
                repository.findByEventTypeAndCommitTimeBetween(
                        "push",
                        start,
                        end
                );


        if (pushEvents == null
                || pushEvents.isEmpty()) {

            return null;
        }


        double totalLeadTime = 0.0;

        int matchedChanges = 0;


        // Process every push event

        for (GitHubEvent pushEvent : pushEvents) {

            String commitSha =
                    pushEvent.getCommitSha();


            if (commitSha == null) {
                continue;
            }


            // Calculate lead time for this commit

            Double leadTime =
                    getLeadTimeForChange(commitSha);


            if (leadTime == null) {
                continue;
            }


            // Ignore invalid negative values

            if (leadTime < 0) {
                continue;
            }


            totalLeadTime += leadTime;

            matchedChanges++;
        }


        if (matchedChanges == 0) {
            return null;
        }


        return totalLeadTime / matchedChanges;
    }


    // =================================================
    // MTTR - MEAN TIME TO RECOVERY
    // =================================================

    /*
     *
     * MTTR =
     *
     * Recovery Time - Failure Time
     *
     * For this project, we use a simplified approach:
     *
     * 1. Find failed deployment workflows.
     *
     * 2. For every failed deployment, find the next
     *    successful deployment for the same repository
     *    and branch.
     *
     * 3. Calculate:
     *
     *    successful deployment time - failure time
     *
     * 4. Take the average of all recovery times.
     *
     *
     * NOTE:
     * This is a simplified proxy for MTTR.
     *
     * In a production system, recovery should ideally
     * be determined using an incident/recovery signal
     * such as an incident management system.
     *
     */

    public Double getMeanTimeToRecovery(
            Instant start,
            Instant end
    ) {

        // Find all failed deployments in the
        // requested time range.

        List<GitHubEvent> failedDeployments =
                repository
                        .findByDeploymentTrueAndWorkflowConclusionAndDeploymentTimeBetween(
                                "failure",
                                start,
                                end
                        );


        // If there are no failed deployments,
        // MTTR cannot be calculated.

        if (failedDeployments == null
                || failedDeployments.isEmpty()) {

            return null;
        }


        double totalRecoveryTime = 0.0;

        int recoveredIncidents = 0;


        // Process every failed deployment

        for (GitHubEvent failedDeployment :
                failedDeployments) {


            // Failure time

            Instant failureTime =
                    failedDeployment.getFailureTime();


            // Repository

            String repositoryName =
                    failedDeployment.getRepository();


            // Branch

            String branch =
                    failedDeployment.getBranch();


            // If required information is missing,
            // we cannot calculate MTTR for this failure.

            if (failureTime == null
                    || repositoryName == null
                    || branch == null) {

                continue;
            }


            // Find successful deployments after
            // this failure for the same repository
            // and branch.

            List<GitHubEvent> successfulDeployments =
                    repository
                            .findByRepositoryAndBranchAndDeploymentTrueAndWorkflowConclusionAndDeploymentTimeAfterOrderByDeploymentTimeAsc(
                                    repositoryName,
                                    branch,
                                    "success",
                                    failureTime
                            );


            // No recovery found yet.

            if (successfulDeployments == null
                    || successfulDeployments.isEmpty()) {

                continue;
            }


            // Because the repository query sorts by
            // deployment time ascending, the first
            // successful deployment is the next recovery.

            GitHubEvent recoveryDeployment =
                    successfulDeployments.get(0);


            Instant recoveredAt =
                    recoveryDeployment.getRecoveredAt();


            // Safety check

            if (recoveredAt == null) {
                continue;
            }


            // Calculate recovery duration

            Duration recoveryDuration =
                    Duration.between(
                            failureTime,
                            recoveredAt
                    );


            // Ignore invalid negative durations

            if (recoveryDuration.isNegative()) {
                continue;
            }


            // Convert recovery time into minutes

            double recoveryMinutes =
                    recoveryDuration.toSeconds()
                            / 60.0;


            totalRecoveryTime += recoveryMinutes;

            recoveredIncidents++;
        }


        // If failures occurred but none have
        // a matching recovery, MTTR cannot
        // currently be calculated.

        if (recoveredIncidents == 0) {
            return null;
        }


        // Average recovery time

        return totalRecoveryTime
                / recoveredIncidents;
    }


    // =================================================
    // COMPLETE DORA METRICS
    // =================================================

    public DoraMetricsResponse getDoraMetrics(
            Instant start,
            Instant end
    ) {

        long deploymentFrequency =
                getDeploymentFrequency(
                        start,
                        end
                );


        double changeFailureRate =
                getChangeFailureRate(
                        start,
                        end
                );


        Double averageLeadTime =
                getAverageLeadTime(
                        start,
                        end
                );


        // Calculate MTTR

        Double mttr =
                getMeanTimeToRecovery(
                        start,
                        end
                );


        return new DoraMetricsResponse(
                deploymentFrequency,
                changeFailureRate,
                averageLeadTime,
                mttr
        );
    }
}