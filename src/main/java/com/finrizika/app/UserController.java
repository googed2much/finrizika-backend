package com.finrizika.app;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.Data;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // -----------------------------------------------------------------------
    @Data
    public static class UserDTO{
        private Long id;
        private String email;
        private String telephone;
        private String fullname;
        private String personId;
        private Role role;

        public UserDTO(User user){
            this.id = user.getId();
            this.email = user.getEmail();
            this.telephone = user.getTelephone();
            this.fullname = user.getFullname();
            this.personId = user.getPersonId();
            this.role = user.getRole();
        }
    }

    // GET request'as. Atiduoda dabartini sesijoje issaugota user.
    @GetMapping("/getme")
    public ResponseEntity<?> getCurrentUser(HttpSession session){
        Long id = (Long) session.getAttribute("id");
        Optional<User> userById = userService.getUserById(id);
        if(!userById.isPresent()) return ResponseEntity.notFound().build();
        
        User user = userById.get();
        return ResponseEntity.ok(new UserDTO(user));
    }

    // GET request'as. Atiduoda viska apie visus userius isskyrus slaptika.
    @GetMapping("/get")
    public ResponseEntity<?> getUsers(HttpSession session){
        Long userId = (Long) session.getAttribute("id");
        boolean authorized = userService.authorize(userId, Role.ADMINISTRATOR);
        if(!authorized) return ResponseEntity.status(401).body("User not authorized");

        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users.stream().map(UserDTO::new).collect(Collectors.toList()));
    }
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    @Data
    public static class RequestUpdateUser{
        @jakarta.validation.constraints.NotNull()
        private Long id;
        @jakarta.validation.constraints.NotBlank()
        private String email;
        @jakarta.validation.constraints.NotBlank()
        private String password;
        @jakarta.validation.constraints.NotBlank()
        private String telephone;
        @jakarta.validation.constraints.NotBlank()
        private String fullname;
        @jakarta.validation.constraints.NotBlank()
        private String personId;
        @jakarta.validation.constraints.NotNull()
        private Role role;

        public RequestUpdateUser() { }
    }

    // PUT requestas. Updatina user'io informacija
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(HttpSession session, @Valid @RequestBody RequestUpdateUser data){
        Long id = (Long) session.getAttribute("id");
        boolean isAdmin = userService.authorize(id, Role.ADMINISTRATOR);

        if(!isAdmin && !data.getId().equals(id)){
            return ResponseEntity.status(401).body("Not authorized");
        }
    
        userService.updateUser(data.getId(), data.getEmail(), data.getPassword(), data.getFullname(), data.getPersonId(), data.getRole());
        return ResponseEntity.ok("Changed successfully");
    }

    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    @Data
    public static class RequestCreateUser{
        @jakarta.validation.constraints.NotBlank()
        private String email;
        @jakarta.validation.constraints.NotBlank()
        private String password;
        @jakarta.validation.constraints.NotBlank()
        private String telephone;
        @jakarta.validation.constraints.NotBlank()
        private String fullname;
        @jakarta.validation.constraints.NotBlank()
        private String personId;
        @jakarta.validation.constraints.NotNull()
        private Role role;

        public RequestCreateUser() { }
    }

    // POST request -> /api/users/create.
    @PostMapping(value = "/create")
    public ResponseEntity<?> createUser(HttpSession session, @Valid @RequestBody RequestCreateUser data){
        Long userId = (Long) session.getAttribute("id");
        boolean authorized = userService.authorize(userId, Role.ADMINISTRATOR);
        if(!authorized) return ResponseEntity.status(401).body("User not authorized");

        userService.createUser(data.getEmail(), data.getPassword(), data.getTelephone(), data.getFullname(), data.getPersonId(), data.getRole());
        return ResponseEntity.ok("User creation successful");
    }
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    @Data
    public static class RequestLoginUser {
        @jakarta.validation.constraints.NotBlank()
        private String email;
        @jakarta.validation.constraints.NotBlank()
        private String password;

        public RequestLoginUser() {
        }
    }

    // POST request -> /api/users/login. Body su "email", "password". Perduoda COOKIE.
    @PostMapping(value = "/login")
    public ResponseEntity<?> login(HttpServletRequest request, @Valid @RequestBody RequestLoginUser data) {
        Optional<User> user = userService.authenticate(data.getEmail(), data.getPassword());
        if (!user.isPresent())
            return ResponseEntity.status(401).body("Wrong email or password");

        HttpSession session = request.getSession(true);
        session.setAttribute("id", user.get().getId());
        return ResponseEntity.ok("Login successful");
    }
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // GET request'as. Nieko nereikia, tiesiog istrina sesija.
    @GetMapping(value = "/logout")
    public ResponseEntity<?> logout(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        session.invalidate();
        return ResponseEntity.ok("Goodbye");
    }
    // -----------------------------------------------------------------------
}