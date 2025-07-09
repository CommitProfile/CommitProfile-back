package goormton.univ.Repo.Controller;

import goormton.univ.Repo.Dto.GitHubRequest;
import goormton.univ.Repo.Dto.GitHubResponse;
import goormton.univ.Repo.Service.GitHubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Repo")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @PostMapping
    public ResponseEntity<List<GitHubResponse>> fetchRepositoriesInfo(@RequestBody GitHubRequest request) throws Exception {
        List<GitHubResponse> responseList = gitHubService.getRepositoriesInfo(request);
        return ResponseEntity.ok(responseList);
    }
}
