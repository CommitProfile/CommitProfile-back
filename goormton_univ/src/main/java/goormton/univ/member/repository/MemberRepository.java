package goormton.univ.member.repository;

import goormton.univ.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByNickName(String nickName);
    boolean existsByEmail(String email);
    boolean existsByNickName(String nickName);
    Optional<Member> findByGithubId(String githubId);
    boolean existsByGithubId(String githubId);
}
