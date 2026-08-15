package com.example.demo.service;

import com.example.demo.model.GitHubEvent;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Service
public class GitHubEventProcessor {

    private final ObjectMapper objectMapper;
    private final GitHubEventService eventService;

    public GitHubEventProcessor(
            ObjectMapper objectMapper,
            GitHubEventService eventService
    ) {
        this.objectMapper = objectMapper;
        this.eventService = eventService;
    }

    // =================================================
    // MAIN EVENT PROCESSOR
    // =================================================

    public void process(
            String eventType,
            String payload
    ) throws Exception {

        // Convert JSON string into JsonNode
        JsonNode json =
                objectMapper.readTree(payload);

        // Create our internal event object
        GitHubEvent event =
                new GitHubEvent();

        // Common fields
        event.setEventType(eventType);

        event.setReceivedAt(
                Instant.now()
        );

        // -----------------------------------------
        // Extract action
        // -----------------------------------------

        if (json.has("action")) {

            event.setAction(
                    json.get("action").asString()
            );
        }

        // -----------------------------------------
        // Handle Pull Request event
        // -----------------------------------------

        if ("pull_request".equals(eventType)) {

            processPullRequest(
                    json,
                    event
            );
        }

        // -----------------------------------------
        // Handle Push event
        // -----------------------------------------

        else if ("push".equals(eventType)) {

            processPush(
                    json,
                    event
            );
        }

        // -----------------------------------------
        // Handle Workflow Run event
        // -----------------------------------------

        else if ("workflow_run".equals(eventType)) {

            processWorkflowRun(
                    json,
                    event
            );
        }

        // -----------------------------------------
        // Save event to MongoDB
        // -----------------------------------------

        eventService.saveEvent(event);

        // -----------------------------------------
        // Debugging
        // -----------------------------------------

        System.out.println(
                "Event processed successfully"
        );

        System.out.println(
                "Event Type: "
                        + event.getEventType()
        );

        System.out.println(
                "Action: "
                        + event.getAction()
        );

        System.out.println(
                "Developer: "
                        + event.getDeveloper()
        );

        System.out.println(
                "Repository: "
                        + event.getRepository()
        );

        System.out.println(
                "PR Number: "
                        + event.getPullRequestNumber()
        );

        System.out.println(
                "Merged: "
                        + event.getMerged()
        );

        System.out.println(
                "Branch: "
                        + event.getBranch()
        );

        System.out.println(
                "Commit Count: "
                        + event.getCommitCount()
        );

        System.out.println(
                "Workflow Name: "
                        + event.getWorkflowName()
        );

        System.out.println(
                "Workflow Status: "
                        + event.getWorkflowStatus()
        );

        System.out.println(
                "Workflow Conclusion: "
                        + event.getWorkflowConclusion()
        );

        System.out.println(
                "Commit SHA: "
                        + event.getCommitSha()
        );

        System.out.println(
                "Deployment: "
                        + event.getDeployment()
        );
    }


    // =================================================
    // PULL REQUEST PROCESSING
    // =================================================

    private void processPullRequest(
            JsonNode json,
            GitHubEvent event
    ) {

        JsonNode pullRequest =
                json.get("pull_request");

        if (pullRequest == null) {
            return;
        }

        // -----------------------------------------
        // Developer
        // -----------------------------------------

        JsonNode user =
                pullRequest.get("user");

        if (user != null
                && user.has("login")) {

            event.setDeveloper(
                    user.get("login").asString()
            );
        }

        // -----------------------------------------
        // Pull Request Number
        // -----------------------------------------

        if (pullRequest.has("number")) {

            event.setPullRequestNumber(
                    pullRequest
                            .get("number")
                            .asInt()
            );
        }

        // -----------------------------------------
        // Merged Status
        // -----------------------------------------

        if (pullRequest.has("merged")) {

            event.setMerged(
                    pullRequest
                            .get("merged")
                            .asBoolean()
            );
        }

        // -----------------------------------------
        // Repository
        // -----------------------------------------

        JsonNode repository =
                json.get("repository");

        if (repository != null
                && repository.has("name")) {

            event.setRepository(
                    repository
                            .get("name")
                            .asString()
            );
        }
    }


    // =================================================
    // PUSH EVENT PROCESSING
    // =================================================

    private void processPush(
            JsonNode json,
            GitHubEvent event
    ) {

        // -----------------------------------------
        // Repository
        // -----------------------------------------

        JsonNode repository =
                json.get("repository");

        if (repository != null
                && repository.has("name")) {

            event.setRepository(
                    repository
                            .get("name")
                            .asString()
            );
        }

        // -----------------------------------------
        // Developer / Pusher
        // -----------------------------------------

        JsonNode pusher =
                json.get("pusher");

        if (pusher != null
                && pusher.has("name")) {

            event.setDeveloper(
                    pusher
                            .get("name")
                            .asString()
            );
        }

        // -----------------------------------------
        // Branch
        // -----------------------------------------

        if (json.has("ref")) {

            String ref =
                    json.get("ref").asString();

            /*
             Example:

             refs/heads/main

             We only want:

             main
            */

            if (ref.startsWith("refs/heads/")) {

                String branch =
                        ref.substring(
                                "refs/heads/".length()
                        );

                event.setBranch(branch);
            }
        }

        // -----------------------------------------
        // Commit Count
        // -----------------------------------------

        JsonNode commits =
                json.get("commits");

        if (commits != null
                && commits.isArray()) {

            event.setCommitCount(
                    commits.size()
            );

        } else {

            event.setCommitCount(0);
        }
    }


    // =================================================
    // WORKFLOW RUN PROCESSING
    // =================================================

    private void processWorkflowRun(
            JsonNode json,
            GitHubEvent event
    ) {

        // -----------------------------------------
        // Repository
        // -----------------------------------------

        JsonNode repository =
                json.get("repository");

        if (repository != null
                && repository.has("name")) {

            event.setRepository(
                    repository
                            .get("name")
                            .asString()
            );
        }

        // -----------------------------------------
        // Workflow Run
        // -----------------------------------------

        JsonNode workflowRun =
                json.get("workflow_run");

        if (workflowRun == null) {
            return;
        }

        // -----------------------------------------
        // Workflow Name
        // -----------------------------------------

        if (workflowRun.has("name")) {

            String workflowName =
                    workflowRun
                            .get("name")
                            .asString();

            event.setWorkflowName(
                    workflowName
            );

            // Identify deployment workflow

            if ("Deploy Application"
                    .equals(workflowName)) {

                event.setDeployment(true);

            } else {

                event.setDeployment(false);
            }
        }

        // -----------------------------------------
        // Workflow Status
        // -----------------------------------------

        if (workflowRun.has("status")) {

            event.setWorkflowStatus(
                    workflowRun
                            .get("status")
                            .asString()
            );
        }

        // -----------------------------------------
        // Workflow Conclusion
        // -----------------------------------------

        if (workflowRun.has("conclusion")) {

            event.setWorkflowConclusion(
                    workflowRun
                            .get("conclusion")
                            .asString()
            );
        }

        // -----------------------------------------
        // Branch
        // -----------------------------------------

        if (workflowRun.has("head_branch")) {

            event.setBranch(
                    workflowRun
                            .get("head_branch")
                            .asString()
            );
        }

        // -----------------------------------------
        // Commit SHA
        // -----------------------------------------

        if (workflowRun.has("head_sha")) {

            event.setCommitSha(
                    workflowRun
                            .get("head_sha")
                            .asString()
            );
        }
    }
}