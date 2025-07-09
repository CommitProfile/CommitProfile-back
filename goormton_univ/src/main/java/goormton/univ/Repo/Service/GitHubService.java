package goormton.univ.Repo.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import goormton.univ.Repo.Dto.GitHubRequest;
import goormton.univ.Repo.Dto.GitHubResponse;
import goormton.univ.Repo.Exception.PrivateRepositoryAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class GitHubService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<GitHubResponse> getRepositoriesInfo(GitHubRequest request) throws Exception {
        String profileUrl = request.profileUrl();
        String username = extractUsername(profileUrl);

        HttpRequest repoListRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/users/" + username + "/repos"))
                .header("User-Agent", "JavaApp")
                .build();

        HttpResponse<String> repoListResponse = client.send(repoListRequest, HttpResponse.BodyHandlers.ofString());
        checkRateLimit(repoListResponse);

        if (repoListResponse.statusCode() != 200) {
            throw new IllegalArgumentException("Failed to fetch repositories for user: " + username);
        }

        JsonNode reposNode = mapper.readTree(repoListResponse.body());
        List<GitHubResponse> responses = new ArrayList<>();

        for (JsonNode repoNode : reposNode) {
            String repoName = repoNode.get("name").asText();
            String description = repoNode.has("description") && !repoNode.get("description").isNull()
                    ? repoNode.get("description").asText()
                    : "";

            CompletableFuture<List<String>> commitsFuture = fetchCommitsAsync(username, repoName);
            CompletableFuture<List<String>> issuesFuture = fetchIssuesAsync(username, repoName);
            CompletableFuture<List<String>> readmeFuture = fetchReadmeAsync(username, repoName);
            CompletableFuture<List<String>> pullRequestsFuture = fetchPullRequestsAsync(username, repoName);

            CompletableFuture.allOf(commitsFuture, issuesFuture, readmeFuture, pullRequestsFuture).join();

            GitHubResponse response = new GitHubResponse(
                    repoName,
                    description,
                    commitsFuture.get(),
                    issuesFuture.get(),
                    readmeFuture.get(),
                    pullRequestsFuture.get()
            );
            responses.add(response);
        }

        return responses;
    }

    private String extractUsername(String profileUrl) throws Exception {
        URI uri = new URI(profileUrl);
        String[] segments = uri.getPath().split("/");
        if (segments.length < 2 || segments[1].isBlank())
            throw new IllegalArgumentException("Invalid GitHub profile URL");
        return segments[1];
    }

    @Async
    public CompletableFuture<List<String>> fetchCommitsAsync(String owner, String repo) {
        try {
            List<String> commits = fetchCommits(owner, repo);
            return CompletableFuture.completedFuture(commits);
        } catch (PrivateRepositoryAccessException e) {
            return CompletableFuture.completedFuture(List.of(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(List.of("Failed to fetch commits."));
        }
    }

    @Async
    public CompletableFuture<List<String>> fetchIssuesAsync(String owner, String repo) {
        try {
            List<String> issues = fetchIssues(owner, repo);
            return CompletableFuture.completedFuture(issues);
        } catch (PrivateRepositoryAccessException e) {
            return CompletableFuture.completedFuture(List.of(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(List.of("Failed to fetch issues."));
        }
    }

    @Async
    public CompletableFuture<List<String>> fetchReadmeAsync(String owner, String repo) {
        try {
            List<String> readme = fetchReadme(owner, repo);
            return CompletableFuture.completedFuture(readme);
        } catch (PrivateRepositoryAccessException e) {
            return CompletableFuture.completedFuture(List.of(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(List.of("Failed to fetch README."));
        }
    }

    @Async
    public CompletableFuture<List<String>> fetchPullRequestsAsync(String owner, String repo) {
        try {
            List<String> prs = fetchPullRequests(owner, repo);
            return CompletableFuture.completedFuture(prs);
        } catch (PrivateRepositoryAccessException e) {
            return CompletableFuture.completedFuture(List.of(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(List.of("Failed to fetch pull requests."));
        }
    }

    private void checkRateLimit(HttpResponse<?> response) throws InterruptedException {
        String remainingStr = response.headers().firstValue("X-RateLimit-Remaining").orElse("1");
        int remaining = Integer.parseInt(remainingStr);

        if (remaining == 0) {
            String resetTime = response.headers().firstValue("X-RateLimit-Reset").orElse("0");
            long resetEpoch = Long.parseLong(resetTime);
            long currentEpoch = System.currentTimeMillis() / 1000;
            long waitSeconds = Math.max(1, resetEpoch - currentEpoch);

            long waitMinutes = (waitSeconds + 59) / 60;  // 올림 처리해서 최소 1분 표시

            throw new RuntimeException("죄송합니다. " + waitMinutes + "분 후에 다시 시도해주세요.");
        }
    }

    private List<String> fetchCommits(String owner, String repo) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/repos/" + owner + "/" + repo + "/commits"))
                .header("User-Agent", "JavaApp")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkRateLimit(response);

        if (response.statusCode() == 403 || response.statusCode() == 404) {
            throw new PrivateRepositoryAccessException();
        }

        if (response.statusCode() != 200) return List.of();

        JsonNode node = mapper.readTree(response.body());
        List<String> commits = new ArrayList<>();
        for (JsonNode commitNode : node) {
            commits.add(commitNode.get("commit").get("message").asText());
        }
        return commits;
    }

    private List<String> fetchIssues(String owner, String repo) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/repos/" + owner + "/" + repo + "/issues"))
                .header("User-Agent", "JavaApp")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkRateLimit(response);

        if (response.statusCode() == 403 || response.statusCode() == 404) {
            throw new PrivateRepositoryAccessException();
        }

        if (response.statusCode() != 200) return List.of();

        JsonNode node = mapper.readTree(response.body());
        List<String> issues = new ArrayList<>();
        for (JsonNode issueNode : node) {
            if (issueNode.has("title")) {
                issues.add(issueNode.get("title").asText());
            }
        }
        return issues;
    }

    private List<String> fetchReadme(String owner, String repo) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/repos/" + owner + "/" + repo + "/readme"))
                .header("User-Agent", "JavaApp")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkRateLimit(response);

        if (response.statusCode() == 403 || response.statusCode() == 404) {
            throw new PrivateRepositoryAccessException();
        }

        if (response.statusCode() != 200) {
            return List.of("README not available");
        }

        JsonNode node = mapper.readTree(response.body());

        if (node.has("content") && node.has("encoding") && node.get("encoding").asText().equals("base64")) {
            try {
                String encodedContent = node.get("content").asText().replaceAll("\n", "");
                byte[] decodedBytes = Base64.getDecoder().decode(encodedContent);
                String decoded = new String(decodedBytes);
                return List.of(decoded.split("\\R"));
            } catch (IllegalArgumentException e) {
                return List.of("Failed to decode README content");
            }
        }

        return List.of("README not found or not base64 encoded");
    }

    private List<String> fetchPullRequests(String owner, String repo) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/repos/" + owner + "/" + repo + "/pulls"))
                .header("User-Agent", "JavaApp")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkRateLimit(response);

        if (response.statusCode() == 403 || response.statusCode() == 404) {
            throw new PrivateRepositoryAccessException();
        }

        if (response.statusCode() != 200) return List.of();

        JsonNode node = mapper.readTree(response.body());
        List<String> pullRequests = new ArrayList<>();
        for (JsonNode prNode : node) {
            if (prNode.has("title")) {
                pullRequests.add(prNode.get("title").asText());
            }
        }
        return pullRequests;
    }
}
