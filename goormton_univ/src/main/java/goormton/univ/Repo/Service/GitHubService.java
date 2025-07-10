package goormton.univ.Repo.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import goormton.univ.Repo.Dto.GitHubRequest;
import goormton.univ.Repo.Dto.GitHubResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Service
public class GitHubService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(GitHubService.class.getName());  // 로그를 위한 Logger 추가

    public List<GitHubResponse> getRepositoriesInfo(GitHubRequest request) throws Exception {
        String profileUrl = request.profileUrl();
        String username = extractUsername(profileUrl);
        String token = request.token();

        // 인증 헤더 설정 (토큰이 있으면 추가)
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();

// 토큰이 있으면 'https://api.github.com/user/repos' 사용
        String url = (token != null && !token.isEmpty())
                ? "https://api.github.com/user/repos"
                : "https://api.github.com/users/" + username + "/repos?visibility=all";

        requestBuilder.uri(new URI(url))
                .header("User-Agent", "JavaApp");

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "token " + token);
        }

        HttpRequest repoListRequest = requestBuilder.build();
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

            // 레포지터리가 private인지 public인지 로그 출력
            boolean isPrivate = repoNode.get("private").asBoolean();
            String visibility = isPrivate ? "private" : "public";
            logger.info("Repository " + repoName + " is " + visibility);  // 로그에 출력

            CompletableFuture<List<String>> commitsFuture = fetchCommitsAsync(username, repoName, token);
            CompletableFuture<List<String>> issuesFuture = fetchIssuesAsync(username, repoName, token);
            CompletableFuture<List<String>> readmeFuture = fetchReadmeAsync(username, repoName, token);
            CompletableFuture<List<String>> pullRequestsFuture = fetchPullRequestsAsync(username, repoName, token);

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
    public CompletableFuture<List<String>> fetchCommitsAsync(String owner, String repo, String token) {
        try {
            List<String> commits = fetchCommits(owner, repo, token);
            return CompletableFuture.completedFuture(commits);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(List.of());
        }
    }

    @Async
    public CompletableFuture<List<String>> fetchIssuesAsync(String owner, String repo, String token) {
        try {
            List<String> issues = fetchIssues(owner, repo, token);
            return CompletableFuture.completedFuture(issues);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(List.of());
        }
    }

    @Async
    public CompletableFuture<List<String>> fetchReadmeAsync(String owner, String repo, String token) {
        try {
            List<String> readme = fetchReadme(owner, repo, token);
            return CompletableFuture.completedFuture(readme);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(List.of());
        }
    }

    @Async
    public CompletableFuture<List<String>> fetchPullRequestsAsync(String owner, String repo, String token) {
        try {
            List<String> prs = fetchPullRequests(owner, repo, token);
            return CompletableFuture.completedFuture(prs);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(List.of());
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

    private List<String> fetchCommits(String owner, String repo, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/repos/" + owner + "/" + repo + "/commits"))
                .header("User-Agent", "JavaApp");

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "token " + token);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkRateLimit(response);

        if (response.statusCode() == 403 || response.statusCode() == 404) {
            throw new Exception();
        }

        if (response.statusCode() != 200) return List.of();

        JsonNode node = mapper.readTree(response.body());
        List<String> commits = new ArrayList<>();
        for (JsonNode commitNode : node) {
            commits.add(commitNode.get("commit").get("message").asText());
        }
        return commits;
    }

    private List<String> fetchIssues(String owner, String repo, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/repos/" + owner + "/" + repo + "/issues"))
                .header("User-Agent", "JavaApp");

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "token " + token);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkRateLimit(response);

        if (response.statusCode() == 403 || response.statusCode() == 404) {
            throw new Exception("This repository may be private or inaccessible.");
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

    private List<String> fetchReadme(String owner, String repo, String token) throws Exception {
        // Readme 정보를 가져오기 위한 API 호출
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/repos/" + owner + "/" + repo + "/contents/README.md"))
                .header("User-Agent", "JavaApp");

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "token " + token);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkRateLimit(response);

        if (response.statusCode() == 403 || response.statusCode() == 404) {
            throw new Exception("This repository may be private or inaccessible.");
        }

        if (response.statusCode() != 200) {
            return List.of();
        }

        JsonNode node = mapper.readTree(response.body());
        List<String> readme = new ArrayList<>();

        // Base64로 인코딩된 경우 처리
        if (node.has("content") && node.has("encoding") && node.get("encoding").asText().equals("base64")) {
            try {
                String encodedContent = node.get("content").asText().replaceAll("\n", "");
                byte[] decodedBytes = Base64.getDecoder().decode(encodedContent);
                String decoded = new String(decodedBytes, StandardCharsets.UTF_8);

                // 개행을 기준으로 내용을 분리하여 리스트로 반환
                String[] lines = decoded.split("\n");
                for (String line : lines) {
                    readme.add(line);
                }
            } catch (IllegalArgumentException e) {
                readme.add("Failed to decode README content");
            }
        } else if (node.has("content")) {
            // Base64로 인코딩되지 않은 경우 원본 텍스트 반환
            String content = node.get("content").asText();
            String[] lines = content.split("\n");
            for (String line : lines) {
                readme.add(line);
            }
        } else {
            readme.add("README not found or not base64 encoded");
        }

        return readme;
    }



    private List<String> fetchPullRequests(String owner, String repo, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/repos/" + owner + "/" + repo + "/pulls"))
                .header("User-Agent", "JavaApp");

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "token " + token);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkRateLimit(response);

        if (response.statusCode() == 403 || response.statusCode() == 404) {
            throw new Exception("This repository may be private or inaccessible.");
        }

        if (response.statusCode() != 200) return List.of();

        JsonNode node = mapper.readTree(response.body());
        List<String> prs = new ArrayList<>();
        for (JsonNode prNode : node) {
            prs.add(prNode.get("title").asText());
        }
        return prs;
    }
}
