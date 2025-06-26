package goormton.univ.member.repository;

import goormton.univ.member.entity.Member;
import goormton.univ.member.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMember(Member member);
    Optional<RefreshToken> findByToken(String token);
    void deleteByMember(Member member);
}
