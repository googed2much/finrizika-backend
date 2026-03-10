package com.finrizika.app;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // -----------------------------------------------------------------------
    // Autorizacija veiksmams. Perduoti request'a ir parinkti, kuria role reikia atitikti.
    public Optional<User> authenticate(String email, String password) {
        return userRepository.findByEmail(email).filter(user -> encoder.matches(password, user.getPassword()));
    }
    // -----------------------------------------------------------------------

    public ResponseEntity<?> createUser(String email, String rawPassword, String telephone, Role role){
        User user = new User();
        user.setEmail(email);
        user.setPassword(encoder.encode(rawPassword));
        user.setTelephone(telephone);
        user.setRole(role);
        userRepository.save(user);
        return ResponseEntity.ok("User creation successful");
    }
    // -----------------------------------------------------------------------


}
