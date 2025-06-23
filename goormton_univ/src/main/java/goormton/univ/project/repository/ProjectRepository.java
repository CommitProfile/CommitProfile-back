package goormton.univ.project.repository;

import goormton.univ.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByPeriodContaining(String keyword); //기간 조회


}
