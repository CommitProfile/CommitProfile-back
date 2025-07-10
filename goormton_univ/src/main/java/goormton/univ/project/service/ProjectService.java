package goormton.univ.project.service;

import goormton.univ.member.entity.Member;
import goormton.univ.member.repository.MemberRepository;
import goormton.univ.project.domain.Project;
import goormton.univ.project.dto.ProjectRequest;
import goormton.univ.project.dto.ProjectDetailResponse;
import goormton.univ.project.dto.ProjectSummaryResponse;
import goormton.univ.project.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .features(request.getFeatures())
                .contributionDescriptions(request.getContributionDescriptions())
                .member(member)
                .build();

        return projectRepository.save(project).getId();
    }

    public ProjectDetailResponse searchById(Long id) {
        return toDetailResponseDTO(projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 프로젝트가 존재하지 않습니다.")));
    }

    //전체 조회
    public List<ProjectSummaryResponse> searchAll() {
        return projectRepository.findAll().stream()
                .map(this::toSummaryResponseDTO)
                .collect(Collectors.toList());
    }

    // 기간 조회
    public List<ProjectSummaryResponse> searchByYear(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        List<Project> result = projectRepository.findByStartDateBetween(start, end);

        if (result.isEmpty()) {
            throw new EntityNotFoundException("해당 연도의 프로젝트가 존재하지 않습니다.");
        }

        return result.stream().map(this::toSummaryResponseDTO).collect(Collectors.toList());
    }


    // 수정
    public void updateProject(Long id, ProjectRequest request) {
        validateRequest(request);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 프로젝트가 존재하지 않습니다."));

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 멤버가 존재하지 않습니다."));

        project.updateFrom(request, member, project.getServiceLogoPath());
    }


    // 삭제
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 프로젝트가 존재하지 않습니다."));
        projectRepository.delete(project);
    }


    // 내부 변환 메서드
    private ProjectDetailResponse toDetailResponseDTO(Project project) {
        return ProjectDetailResponse.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .teamName(project.getTeamName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .features(project.getFeatures())
                .contributionDescriptions(project.getContributionDescriptions())
                .serviceLogoPath(project.getServiceLogoPath())
                .build();
    }


    private ProjectSummaryResponse toSummaryResponseDTO(Project project) {
        LocalDate start = project.getStartDate();
        LocalDate end = project.getEndDate();

        String period;
        if (start != null && end != null) {
            period = String.format("%02d.%02d ~ %02d.%02d",
                    start.getYear() % 100, start.getMonthValue(),
                    end.getYear() % 100, end.getMonthValue());
        } else {
            period = "기간 정보 없음";  // 또는 throw new IllegalArgumentException(...)
        }

        return ProjectSummaryResponse.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .teamName(project.getTeamName())
                .period(period)
                .description(project.getDescription())
                .serviceLogoPath(project.getServiceLogoPath())
                .contributionDescriptions(project.getContributionDescriptions())
                .build();
    }


    private void validateRequest(ProjectRequest request) {
        if (request.getProjectName() == null || request.getProjectName().trim().isEmpty()
                || request.getMemberId() == null) {
            throw new IllegalArgumentException("입력값 중 유효하지 않은 항목이 있습니다.");
        }
    }
}