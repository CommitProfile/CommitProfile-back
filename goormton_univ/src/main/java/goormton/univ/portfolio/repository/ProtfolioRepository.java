package goormton.univ.portfolio.repository;

import goormton.univ.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProtfolioRepository extends JpaRepository<Portfolio, Long> {
}
