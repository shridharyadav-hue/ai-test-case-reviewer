package com.example.aitestreviewer.application;

import com.example.aitestreviewer.application.port.out.GitHubPort;
import com.example.aitestreviewer.application.port.out.SonarQubePort;
import com.example.aitestreviewer.domain.CodeCoverage;
import com.example.aitestreviewer.domain.PullRequestInfo;
import com.example.aitestreviewer.domain.ReviewComment;
import com.example.aitestreviewer.domain.TestQualityScore;

public class TestReviewAgent {

    private final SonarQubePort sonarQubePort;
    private final GitHubPort gitHubPort;
    private final ReviewService reviewService;
    private final TestQualityScoringService scoringService;

    public TestReviewAgent(
            SonarQubePort sonarQubePort,
            GitHubPort gitHubPort,
            ReviewService reviewService,
            TestQualityScoringService scoringService
    ) {
        this.sonarQubePort = sonarQubePort;
        this.gitHubPort = gitHubPort;
        this.reviewService = reviewService;
        this.scoringService = scoringService;
    }

    public AgentOutput review(String projectKey, String repository, String pullRequestId, int assertions, int edgeCases, int exceptionHandling, int mockitoScore) {
        CodeCoverage coverage = sonarQubePort.fetchCoverage(projectKey);
        PullRequestInfo pullRequest = gitHubPort.fetchPullRequest(repository, pullRequestId);
        ReviewComment review = reviewService.review(coverage, pullRequest);
        TestQualityScore score = scoringService.score(coverage, assertions, edgeCases, exceptionHandling, mockitoScore);
        return new AgentOutput(review, score);
    }

    public record AgentOutput(ReviewComment reviewComment, TestQualityScore qualityScore) {
    }
}

