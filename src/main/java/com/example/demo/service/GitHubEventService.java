package com.example.demo.service;

import com.example.demo.dto.DeveloperAnalyticsResponse;
import com.example.demo.model.GitHubEvent;
import com.example.demo.repository.GitHubEventRepository;

import org.springframework.stereotype.Service;

@Service
public class GitHubEventService {

    private final GitHubEventRepository repository;

    public GitHubEventService(
            GitHubEventRepository repository
    ) {
        this.repository = repository;
    }

    // ---------------------------------------
    // Save GitHub event
    // ---------------------------------------

    public GitHubEvent saveEvent(
            GitHubEvent event
    ) {

        return repository.save(event);
    }


    // ---------------------------------------
    // Count PRs opened
    // ---------------------------------------

    public long countPRsOpened(
            String developer
    ) {

        return repository
                .countByDeveloperAndEventTypeAndAction(
                        developer,
                        "pull_request",
                        "opened"
                );
    }


    // ---------------------------------------
    // Count PRs merged
    // ---------------------------------------

    public long countPRsMerged(
            String developer
    ) {

        return repository
                .countByDeveloperAndEventTypeAndActionAndMergedTrue(
                        developer,
                        "pull_request",
                        "closed"
                );
    }


    // ---------------------------------------
    // Count PRs closed without merging
    // ---------------------------------------

    public long countPRsClosed(
            String developer
    ) {

        return repository
                .countByDeveloperAndEventTypeAndActionAndMergedFalse(
                        developer,
                        "pull_request",
                        "closed"
                );
    }


    // ---------------------------------------
    // Count commits
    // ---------------------------------------

    public long countCommits(
            String developer
    ) {

        Integer total =
                repository.sumCommitsByDeveloper(
                        developer
                );

        return total != null ? total : 0;
    }


    // ---------------------------------------
    // Get complete developer analytics
    // ---------------------------------------

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
}