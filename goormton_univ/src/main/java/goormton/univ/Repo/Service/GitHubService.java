package goormton.univ.Repo.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import goormton.univ.Repo.Dto.GitHubRequest;
import goormton.univ.Repo.Dto.GitHubResponse;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class GitHubService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public GitHubResponse getRepositoryInfo(GitHubRequest request) throws Exception {
        String repoUrl = request.repoUrl();
        String[] parts = repoUrl.replace("https://github.com/", "").split("/");
        if (parts.length < 2) throw new IllegalArgumentException("Invalid GitHub URL");

        String owner = parts[0];
        String repo = parts[1];

        List<String> commits = fetchCommits(owner, repo);
        List<String> issues = fetchIssues(owner, repo);
        List<String> readme = fetchReadme(owner, repo);
        String description = fetchDescription(owner, repo);
        List<String> pullRequests = fetchPullRequests(owner, repo);


        return new GitHubResponse(repo, description, commits, issues, readme, pullRequests);

    }

    private List<String> fetchCommits(String owner, String repo) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/repos/" + owner + "/" + repo + "/commits"))
                .header("User-Agent", "JavaApp")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

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

        if (response.statusCode() != 200) {
            return List.of("README not available");
        }

        JsonNode node = mapper.readTree(response.body());

        if (node.has("content") && node.has("encoding") && node.get("encoding").asText().equals("base64")) {
            try {
                String encodedContent = node.get("content").asText().replaceAll("\n", "");
                byte[] decodedBytes = Base64.getDecoder().decode(encodedContent);
                String decoded = new String(decodedBytes); // 기본 UTF-8 사용

                // 줄 단위로 나눠 리스트 반환
                return List.of(decoded.split("\\R")); // \R은 모든 줄바꿈 문자(\n, \r\n 등)에 대응
            } catch (IllegalArgumentException e) {
                return List.of("Failed to decode README content");
            }
        }

        return List.of("README not found or not base64 encoded");
    }



    private String fetchDescription(String owner, String repo) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/repos/" + owner + "/" + repo))
                .header("User-Agent", "JavaApp")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) return "";

        JsonNode node = mapper.readTree(response.body());
        return node.has("description") ? node.get("description").asText() : "";
    }
    private List<String> fetchPullRequests(String owner, String repo) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/repos/" + owner + "/" + repo + "/pulls"))
                .header("User-Agent", "JavaApp")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

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
