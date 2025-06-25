package goormton.univ.project.Repo.Controller;

import goormton.univ.project.Repo.Dto.GitHubRequest;
import goormton.univ.project.Repo.Dto.GitHubResponse;
import goormton.univ.project.Repo.Service.GitHubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Repo")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @PostMapping
    public ResponseEntity<GitHubResponse> fetchRepositoryInfo(@RequestBody GitHubRequest request) {
        try {
            GitHubResponse response = gitHubService.getRepositoryInfo(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
