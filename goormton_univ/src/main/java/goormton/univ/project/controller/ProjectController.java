package goormton.univ.project.controller;

import goormton.univ.project.dto.ProjectRequest;
import goormton.univ.project.dto.ProjectResponse;
import goormton.univ.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor

public class ProjectController {
    private final ProjectService projectService;

    @PostMapping("/info")
    public ResponseEntity<Long> createProject(@RequestBody ProjectRequest request){
        Long projectId = projectService.saveProject(request);
        return ResponseEntity.ok(projectId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id){
        ProjectResponse response = projectService.searchById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProjectResponse>> getAllProjects(){
        List<ProjectResponse> responseList = projectService.searchAll();
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/search") //예시 search?keyword=2023.11
    public ResponseEntity<List<ProjectResponse>> searchByPeriod(@RequestParam String keyword) {
        List<ProjectResponse> responseList = projectService.searchByPeriod(keyword);
        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<Void> updateProject(@PathVariable Long id, @RequestBody ProjectRequest request) {
        projectService.updateProject(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok().build();
    }

}
