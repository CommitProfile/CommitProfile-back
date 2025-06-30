package goormton.univ.project.service;

import goormton.univ.member.entity.Member;
import goormton.univ.member.repository.MemberRepository;
import goormton.univ.project.domain.Project;
import goormton.univ.project.dto.ProjectRequest;
import goormton.univ.project.dto.ProjectResponse;
import goormton.univ.project.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;


    public Long saveProject(ProjectRequest request) {
        validateRequest(request);
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 멤버가 존재하지 않습니다."));
        Project project = Project.builder()
                .projectName(request.getProjectName())
                .teamName(request.getTeamName())
                .description(request.getDescription())
                .period(request.getPeriod())
                .techStack(request.getTechStack())
                .githubUrl(request.getGithubUrl())
                .contributionDescription(request.getContributionDescription())
                .member(member)
                .build();

        return projectRepository.save(project).getId();
    }

    public ProjectResponse searchById(Long id) {
        return toResponseDTO(projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 프로젝트가 존재하지 않습니다.")));

    }

    //전체 조회
    public List<ProjectResponse> searchAll() {
        return toDTOList(projectRepository.findAll());
    }


    // 기간 조회
    public List<ProjectResponse> searchByPeriod(String keyword) {
        List<Project> result = projectRepository.findByPeriodContaining(keyword);

        if (result.isEmpty()) {
            throw new EntityNotFoundException("해당 기간의 프로젝트가 존재하지 않습니다.");
        }

        return toDTOList(result);
    }


    // 수정
    public void updateProject(Long id, ProjectRequest request) {
        validateRequest(request);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 프로젝트가 존재하지 않습니다."));

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 멤버가 존재하지 않습니다."));

        project.updateFrom(request, member);
    }


    // 삭제
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 프로젝트가 존재하지 않습니다."));
        projectRepository.delete(project);
    }


    // 내부 변환 메서드
    private ProjectResponse toResponseDTO(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .teamName(project.getTeamName())
                .description(project.getDescription())
                .period(project.getPeriod())
                .techStack(project.getTechStack())
                .githubUrl(project.getGithubUrl())
                .contributionDescription(project.getContributionDescription())
                .memberId(project.getMember().getId())

                .build();
    }


    private List<ProjectResponse> toDTOList(List<Project> projects) {
        return projects.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }


    private void validateRequest(ProjectRequest request) {
        if (request.getProjectName() == null || request.getProjectName().trim().isEmpty()
                || request.getTechStack() == null || request.getTechStack().trim().isEmpty()
                || request.getMemberId() == null) {
            throw new IllegalArgumentException("입력값 중 유효하지 않은 항목이 있습니다.");
        }
    }
}
