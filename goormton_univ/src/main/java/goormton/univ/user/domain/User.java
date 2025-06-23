package goormton.univ.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")  // ★ 반드시 넣어주기 (user는 예약어라서 문제됨)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // user_id
    private String name;


}
