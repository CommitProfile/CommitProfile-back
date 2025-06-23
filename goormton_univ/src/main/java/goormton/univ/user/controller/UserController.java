package goormton.univ.user.controller;

import goormton.univ.user.domain.User;
import goormton.univ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    // User 등록
    @PostMapping
    public ResponseEntity<Long> createUser(@RequestBody User user) {
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser.getId());
    }

}
