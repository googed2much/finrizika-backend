package com.finrizika.app;

import java.util.List;
import java.util.Optional;
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
    // Autorizacija
    public boolean authorize(long id, Role requiredRole){
        Optional<User> userById = userRepository.findById(id);
        if(userById.isEmpty()) return false;

        User user = userById.get();
        Role currentRole = user.getRole();
        if(currentRole.ordinal() >= requiredRole.ordinal()) return true;

        return false;
    }
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // Autentifikacija
    public Optional<User> authenticate(String email, String password) {
        return userRepository.findByEmail(email).filter(user -> encoder.matches(password, user.getPassword()));
    }
    // -----------------------------------------------------------------------

    public void createUser(String email, String rawPassword, String telephone, Role role){
        User user = new User();
        user.setEmail(email);
        user.setPassword(encoder.encode(rawPassword));
        user.setTelephone(telephone);
        user.setRole(role);
        userRepository.save(user);
    }
    // -----------------------------------------------------------------------

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

}
