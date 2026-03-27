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

    private boolean hasValue(String s) {
        return s != null && !s.isBlank();
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

    // -----------------------------------------------------------------------
    public void createUser(String email, String rawPassword, String telephone, String fullname, String personId, Role role){
        User user = new User();
        user.setEmail(email);
        user.setPassword(encoder.encode(rawPassword));
        user.setTelephone(telephone);
        user.setFullname(fullname);
        user.setPersonId(personId);
        user.setRole(role);
        userRepository.save(user);
    }
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    public Optional<User> getUserById(Long id){
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    public void updateUser(Long id, String email, String password, String fullname, String personId, Role role){
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found: " + id));

        if(hasValue(email)) user.setEmail(email);
        if(hasValue(password)) user.setPassword(encoder.encode(password));
        if(hasValue(fullname)) user.setFullname(fullname);
        if(hasValue(personId)) user.setPersonId(personId);
        if(role != null) user.setRole(role);

        userRepository.save(user);
    }
    // -----------------------------------------------------------------------

}
