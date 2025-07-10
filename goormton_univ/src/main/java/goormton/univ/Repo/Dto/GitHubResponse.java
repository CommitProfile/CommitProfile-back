package goormton.univ.Repo.Dto;

import java.util.List;

public record GitHubResponse(
        String repositoryName,
        String description,
        List<String> commits,
        List<String> issues,
        List<String> readme,
        List<String> pullRequests
) {}