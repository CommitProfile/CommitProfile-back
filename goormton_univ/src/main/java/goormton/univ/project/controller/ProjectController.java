package goormton.univ.project.controller;

import goormton.univ.project.dto.ProjectRequest;
import goormton.univ.project.dto.ProjectDetailResponse;
import goormton.univ.project.dto.ProjectSummaryResponse;import goormton.univ.project.service.ProjectService;
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
    public ResponseEntity<String> createProject(@RequestBody ProjectRequest request){
        Long projectId = projectService.saveProject(request);
        return ResponseEntity.ok("프로젝트 생성 완료 (ID: " + projectId + ")");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailResponse> getProject(@PathVariable Long id){
        ProjectDetailResponse response = projectService.searchById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProjectSummaryResponse>> getAllProjects(){
        List<ProjectSummaryResponse> responseList = projectService.searchAll();
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProjectSummaryResponse>> searchByYear(@RequestParam int year) {
        List<ProjectSummaryResponse> responseList = projectService.searchByYear(year);
        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<String> updateProject(@PathVariable Long id, @RequestBody ProjectRequest request) {
        projectService.updateProject(id, request);
        return ResponseEntity.ok("프로젝트 수정 완료");
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<String> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok("프로젝트 삭제 완료");
    }

}
