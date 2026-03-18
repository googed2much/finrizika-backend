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
import lombok.Data;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private boolean isNullOrBlank(String s) {
        return s == null || s.isBlank();
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
    @GetMapping("/get-current")
    public ResponseEntity<?> getCurrentUser(HttpSession session){
        Long id = (Long) session.getAttribute("id");
        if(id == null) return ResponseEntity.notFound().build();

        Optional<User> userById = userService.getUserById(id);
        if(!userById.isPresent()) return ResponseEntity.notFound().build();
        User user = userById.get();
        return ResponseEntity.ok(new UserDTO(user));
    }

    // GET request'as. Atiduoda viska apie visus userius isskyrus slaptika.
    @GetMapping("/get")
    public ResponseEntity<?> getUsers(HttpSession session){
        Long userId = (Long) session.getAttribute("id");
        if (userId == null) return ResponseEntity.status(401).body("User not authorized");
        boolean authorized = userService.authorize(userId, Role.ADMINISTRATOR);
        if(!authorized) return ResponseEntity.status(401).body("User not authorized");

        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users.stream().map(UserDTO::new).collect(Collectors.toList()));
    }
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    @Data
    public static class RequestUpdateUser{
        private Long id;
        private String email;
        private String password;
        private String telephone;
        private String fullname;
        private String personId;
        private Role role;

        public RequestUpdateUser() { }
    }

    // PUT requestas. Updatina user'io informacija
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(HttpSession session, @RequestBody RequestUpdateUser data){
        Long id = (Long) session.getAttribute("id");
        if(id == null) return ResponseEntity.status(401).body("Unauthorized access");
        if(data.getId() == null) return ResponseEntity.badRequest().body("No user ID found");

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
        private String email;
        private String password;
        private String telephone;
        private String fullname;
        private String personId;
        private Role role;

        public RequestCreateUser() { }
    }

    // POST request -> /api/users/create.
    @PostMapping(value = "/create")
    public ResponseEntity<?> createUser(HttpSession session, @RequestBody RequestCreateUser data){
        Long userId = (Long) session.getAttribute("id");
        if (userId == null) return ResponseEntity.status(401).body("User not authorized");
        boolean authorized = userService.authorize(userId, Role.ADMINISTRATOR);
        if(!authorized) return ResponseEntity.status(401).body("User not authorized");

        if(data.getEmail() == null || data.getPassword() == null || data.getTelephone() == null || data.getFullname() == null || data.getPersonId() == null || data.getRole() == null) return ResponseEntity.badRequest().body("Missing parameters");
        userService.createUser(data.getEmail(), data.getPassword(), data.getTelephone(), data.getFullname(), data.getPersonId(), data.getRole());
        return ResponseEntity.ok("User creation successful");
    }
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    @Data
    public static class RequestLoginUser {
        private String email;
        private String password;

        public RequestLoginUser() {
        }
    }

    // POST request -> /api/users/login. Body su "email", "password". Perduoda COOKIE.
    @PostMapping(value = "/login")
    public ResponseEntity<?> login(HttpServletRequest request, @RequestBody RequestLoginUser data) {
        if (isNullOrBlank(data.getEmail()) || isNullOrBlank(data.getPassword()))
            return ResponseEntity.status(401).body("Incorrect request");

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