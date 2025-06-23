package goormton.univ.project.domain;

import goormton.univ.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName;
    private String teamName;
    private String description;
    private String period;
    private String techStack;
    private String githubUrl;
    private String contributionDescription; //작업기여도


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")  // FK
    private User user;


}
