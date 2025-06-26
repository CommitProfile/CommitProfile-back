package goormton.univ.profile.entity;

import goormton.univ.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String introduction;

    private String phoneNumber;

    private String skills;

    private String githubUrl;

    private String blogUrl;

    @OneToOne(mappedBy = "profile")
    private Member member;

    //연관관계 메서드
    public void setMember(Member member) {
        this.member = member;
        if (member.getProfile() != this) {
            member.setProfile(this);
        }
    }
}
