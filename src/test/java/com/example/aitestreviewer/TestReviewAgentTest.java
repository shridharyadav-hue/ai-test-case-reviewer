package com.example.aitestreviewer;

import com.example.aitestreviewer.application.ReviewService;
import com.example.aitestreviewer.application.TestQualityScoringService;
import com.example.aitestreviewer.application.TestReviewAgent;
import com.example.aitestreviewer.application.port.out.GitHubPort;
import com.example.aitestreviewer.application.port.out.PromptProviderPort;
import com.example.aitestreviewer.application.port.out.SonarQubePort;
import com.example.aitestreviewer.domain.CodeCoverage;
import com.example.aitestreviewer.domain.PullRequestInfo;
import com.example.aitestreviewer.domain.ReviewComment;
import com.example.aitestreviewer.domain.TestQualityScore;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestReviewAgentTest {

    @Test
    void shouldReviewProjectAndGenerateRiskSummary() {
        SonarQubePort sonarQubePort = projectKey -> new CodeCoverage(72.5, 18, 9);
        GitHubPort gitHubPort = (repository, pullRequestId) -> new PullRequestInfo(
                pullRequestId,
                "PR-42",
                repository,
                List.of()
        );
        PromptProviderPort promptProvider = template -> "Prompt for: " + template;

        ReviewService reviewService = new ReviewService(promptProvider);
        TestQualityScoringService scoringService = new TestQualityScoringService();
        TestReviewAgent agent = new TestReviewAgent(sonarQubePort, gitHubPort, reviewService, scoringService);

        TestReviewAgent.AgentOutput output = agent.review("demo-project", "example/repo", "42", 10, 4, 3, 75);

        ReviewComment reviewComment = output.reviewComment();
        TestQualityScore score = output.qualityScore();

        assertNotNull(reviewComment);
        assertNotNull(score);
        assertTrue(reviewComment.missingTests().contains("Null Input"));
        assertTrue(score.score() >= 0 && score.score() <= 100);
    }
}

