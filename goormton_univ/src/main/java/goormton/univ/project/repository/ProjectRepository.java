package goormton.univ.project.repository;

import goormton.univ.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByStartDateBetween(LocalDate start, LocalDate end);


}
