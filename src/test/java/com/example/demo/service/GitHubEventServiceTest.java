package com.example.demo.service;

import com.example.demo.dto.DeveloperAnalyticsResponse;
import com.example.demo.model.GitHubEvent;
import com.example.demo.repository.GitHubEventRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubEventServiceTest {

    @Mock
    private GitHubEventRepository repository;

    @InjectMocks
    private GitHubEventService service;


    @Test
    void shouldSaveEvent() {

        GitHubEvent event = new GitHubEvent();

        when(repository.save(event)).thenReturn(event);

        GitHubEvent result = service.saveEvent(event);

        assertEquals(event, result);

        verify(repository).save(event);
    }


    @Test
    void shouldCountPRsOpened() {

        when(repository.countByDeveloperAndEventTypeAndAction(
                "test-developer",
                "pull_request",
                "opened"
        )).thenReturn(5L);

        long result =
                service.countPRsOpened("test-developer");

        assertEquals(5L, result);

        verify(repository).countByDeveloperAndEventTypeAndAction(
                "test-developer",
                "pull_request",
                "opened"
        );
    }


    @Test
    void shouldCountPRsMerged() {

        when(repository.countByDeveloperAndEventTypeAndActionAndMergedTrue(
                "test-developer",
                "pull_request",
                "closed"
        )).thenReturn(3L);

        long result =
                service.countPRsMerged("test-developer");

        assertEquals(3L, result);

        verify(repository).countByDeveloperAndEventTypeAndActionAndMergedTrue(
                "test-developer",
                "pull_request",
                "closed"
        );
    }


    @Test
    void shouldCountPRsClosedWithoutMerging() {

        when(repository.countByDeveloperAndEventTypeAndActionAndMergedFalse(
                "test-developer",
                "pull_request",
                "closed"
        )).thenReturn(2L);

        long result =
                service.countPRsClosed("test-developer");

        assertEquals(2L, result);

        verify(repository).countByDeveloperAndEventTypeAndActionAndMergedFalse(
                "test-developer",
                "pull_request",
                "closed"
        );
    }


    @Test
    void shouldCountCommits() {

        when(repository.sumCommitsByDeveloper(
                "test-developer"
        )).thenReturn(10);

        long result =
                service.countCommits("test-developer");

        assertEquals(10L, result);

        verify(repository).sumCommitsByDeveloper(
                "test-developer"
        );
    }


    @Test
    void shouldReturnZeroWhenCommitSumIsNull() {

        when(repository.sumCommitsByDeveloper(
                "test-developer"
        )).thenReturn(null);

        long result =
                service.countCommits("test-developer");

        assertEquals(0L, result);
    }


    @Test
    void shouldReturnCompleteDeveloperAnalytics() {

        when(repository.countByDeveloperAndEventTypeAndAction(
                "test-developer",
                "pull_request",
                "opened"
        )).thenReturn(5L);

        when(repository.countByDeveloperAndEventTypeAndActionAndMergedTrue(
                "test-developer",
                "pull_request",
                "closed"
        )).thenReturn(3L);

        when(repository.countByDeveloperAndEventTypeAndActionAndMergedFalse(
                "test-developer",
                "pull_request",
                "closed"
        )).thenReturn(1L);

        when(repository.sumCommitsByDeveloper(
                "test-developer"
        )).thenReturn(10);

        DeveloperAnalyticsResponse result =
                service.getDeveloperAnalytics("test-developer");

        assertEquals("test-developer", result.getDeveloper());
        assertEquals(5L, result.getPrsOpened());
        assertEquals(3L, result.getPrsMerged());
        assertEquals(1L, result.getPrsClosed());
        assertEquals(10L, result.getCommits());
    }
}