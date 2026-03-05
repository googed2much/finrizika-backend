package com.finrizika.app;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
public class UserController{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.encoder = new BCryptPasswordEncoder(12);
    }
    
    // -----------------------------------------------------------------------
    // Autorizacija veiksmams. Perduoti request'a ir parinkti, kuria role reikia atitikti.
    public boolean authorize(HttpServletRequest request, Role requiredRole){
        HttpSession session = request.getSession(false);
        if(session == null) return false;

        Long userId = (Long) session.getAttribute("id");
        Role role = (Role) session.getAttribute("role");
        if(userId != null && role != null && role == requiredRole) return role.ordinal() >= requiredRole.ordinal();

        return false;
    }
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    public static class RequestCreateUser{
        private String email;
        private String password;
        private String telephone;

        public RequestCreateUser(){
        }

        // Email
        public String getEmail(){
            return this.email;
        }
        public void setEmail(String email){
            this.email = email;
        }

        // Password
        public String getPassword(){
            return this.password;
        }
        public void setPassword(String password){
            this.password = password;
        }

        // Telephone
        public String getTelephone(){
            return this.telephone;
        }
        public void setTelephone(String telephone){
            this.telephone = telephone;
        }
    }
    // POST request -> /api/v1/user/create. Body su "email", "password", "telephone".
    @PostMapping(value = "/api/v1/user/create")
    public ResponseEntity<?> createUser(@RequestBody RequestCreateUser data){
        User user = new User();
        user.setEmail(data.getEmail());
        user.setPassword(encoder.encode(data.getPassword()));
        user.setTelephone(data.getTelephone());
        user.setRole(Role.INVESTOR);
        userRepository.save(user);
        return ResponseEntity.ok("User creation successful");
    }
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    public static class RequestLoginUser{

        private String email;
        private String password;

        public RequestLoginUser(){
        }

        // Email
        public String getEmail(){
            return this.email;
        }
        public void setEmail(String email){
            this.email = email;
        }

        // Password
        public String getPassword(){
            return this.password;
        }
        public void setPassword(String password){
            this.password = password;
        }

    }
    // POST request -> /api/v1/login. Body su "email", "password". Perduoda COOKIE.
    @PostMapping(value = "/api/v1/login")
    public ResponseEntity<?> login(HttpServletRequest request, @RequestBody RequestLoginUser data){
        if(data.getEmail() == null || data.getPassword() == null) return ResponseEntity.status(401).body("Incorrect request");
        
        Optional<User> userByEmail = userRepository.findByEmail(data.getEmail());
        if(!userByEmail.isPresent()) return ResponseEntity.status(401).body("User not found");
        User user = userByEmail.get();

        boolean matches = encoder.matches(data.getPassword(), user.getPassword());
        if(matches){
            HttpSession session = request.getSession(true);
            session.setAttribute("id", user.getId());
            session.setAttribute("role", user.getRole());
            return ResponseEntity.ok("Login successful");
        }

        return ResponseEntity.status(401).body("Login failed");
    }
    // -----------------------------------------------------------------------
}