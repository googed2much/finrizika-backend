package com.finrizika.app;

import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.finrizika.app.UserController.UserDTO;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // -----------------------------------------------------------------------
    // Autentifikacija
    public User authenticate(String email, String password) {
        return userRepository.findByEmail(email).filter(user -> encoder.matches(password, user.getPassword())).orElseThrow(() -> new EntityNotFoundException("Wrong email or password."));
    }

    // Autorizacija
    public boolean authorize(Long id, Role requiredRole){
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Role currentRole = user.getRole();
        if(currentRole.ordinal() >= requiredRole.ordinal()) return true;
        return false;
    }
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    public Long createUser(UserDTO dto){
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setTelephone(dto.getTelephone());
        user.setFullname(dto.getFullname());
        user.setCitizenId(dto.getCitizenId());
        user.setRole(dto.getRole());
        User saved = userRepository.save(user);
        return saved.getId();
    }
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    public User getUserById(Long id){
        return userRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("User not found."));
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    public Long updateUser(UserDTO dto){
        User user = userRepository.findById(dto.getId()).orElseThrow(() -> new RuntimeException("User not found: " + dto.getId()));
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setTelephone(dto.getTelephone());
        user.setFullname(dto.getFullname());
        user.setCitizenId(dto.getCitizenId());
        user.setRole(dto.getRole());
        User saved = userRepository.save(user);
        return saved.getId();
    }
    // -----------------------------------------------------------------------

}
