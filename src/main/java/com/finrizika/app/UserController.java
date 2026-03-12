package com.finrizika.app;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // -----------------------------------------------------------------------
    public static class RequestCreateUser {
        private String email;
        private String password;
        private String telephone;

        public RequestCreateUser() {
        }

        // Email
        public String getEmail() {
            return this.email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        // Password
        public String getPassword() {
            return this.password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        // Telephone
        public String getTelephone() {
            return this.telephone;
        }

        public void setTelephone(String telephone) {
            this.telephone = telephone;
        }
    }

    // POST request -> /api/users/create. Body su "email", "password", "telephone".
    @PostMapping(value = "/create")
    public ResponseEntity<?> createUser(HttpServletRequest request, @RequestBody RequestCreateUser data) {
        if (data.getEmail() == null || data.getPassword() == null || data.getTelephone() == null)
            return ResponseEntity.badRequest().body("Missing parameters");
        userService.createUser(data.getEmail(), data.getPassword(), data.getTelephone(), Role.INVESTOR);
        return ResponseEntity.ok("User creation successful");
    }
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    public static class RequestLoginUser {

        private String email;
        private String password;

        public RequestLoginUser() {
        }

        // Email
        public String getEmail() {
            return this.email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        // Password
        public String getPassword() {
            return this.password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }

    // POST request -> /api/users/login. Body su "email", "password". Perduoda
    // COOKIE.
    @PostMapping(value = "/login")
    public ResponseEntity<?> login(HttpServletRequest request, @RequestBody RequestLoginUser data) {
        if (data.getEmail() == null || data.getPassword() == null)
            return ResponseEntity.status(401).body("Incorrect request");

        Optional<User> user = userService.authenticate(data.getEmail(), data.getPassword());
        if (!user.isPresent())
            return ResponseEntity.status(401).body("Wrong email or password");

        HttpSession session = request.getSession(true);
        session.setAttribute("id", user.get().getId());
        session.setAttribute("role", user.get().getRole());
        return ResponseEntity.ok("Login successful");
    }
    // -----------------------------------------------------------------------
}