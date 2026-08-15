package com.example.demo.repository;

import com.example.demo.model.GitHubEvent;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GitHubEventRepository
        extends MongoRepository<GitHubEvent, String> {

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
Integer sumCommitsByDeveloper(String developer);
}