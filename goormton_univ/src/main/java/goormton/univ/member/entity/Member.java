package goormton.univ.member.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import goormton.univ.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Column(nullable = false, unique = true)
    private String nickName;

    @Column
    private String name;

    @Column(name = "profile_image_url", length = 1024)
    private String profileImageUrl;

    @Column(name = "github_id")
    private String githubId;

    @Column(name = "github_login")
    private String githubLogin;

    @Column(name = "provider", nullable = false)
    @Builder.Default
    private String provider = "LOCAL";

    @Column(name = "created_at", updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SS")
    private LocalDateTime createdAt;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Profile profile;

    @PrePersist
    protected void onCreate() {
        this.createdAt = java.time.LocalDateTime.now();
    }

    public Member(String email, String password, String nickName, String profileImageUrl, String name) {
        this.email = email;
        this.password = password;
        this.nickName = nickName;
        this.profileImageUrl = profileImageUrl;
        this.name = name;
    }

    public static Member createGithubMember(String email, String nickname, String name, String profileImage, String githubId, String githubLogin){
        return Member.builder()
                .email(email)
                .password(null)
                .nickName(nickname)
                .name(name)
                .profileImageUrl(profileImage)
                .githubId(githubId)
                .provider("GITHUB")
                .build();
    }

    //연관관계 메서드
    public void setProfile(Profile profile) {
        this.profile = profile;
        if (profile.getMember() != this) {
            profile.setMember(this);
        }
    }

    public boolean isGithubUser() {
        return "GITHUB".equals(this.provider);
    }

    public boolean isLocalUser() {
        return "LOCAL".equals(this.provider);
    }
}