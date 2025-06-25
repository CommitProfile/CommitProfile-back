package goormton.univ.profile.repository;

import goormton.univ.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // 프로필을 memberId로 조회하는 메소드
     Optional<Profile> findByMemberId(Long memberId);

}
