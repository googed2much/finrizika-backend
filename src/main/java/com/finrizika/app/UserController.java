package com.finrizika.app;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Value("${app.dev-mode:false}")
    private boolean DEV_MODE;

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // -----------------------------------------------------------------------
    // DTO classes
    // -----------------------------------------------------------------------

    private interface OnCreate {}
    private interface OnUpdate {}

    @Getter
    @Setter
    public static class UserDTO{
        @NotNull(groups = {OnUpdate.class})
        private Long id;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String email;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String password;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String telephone;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String fullname;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String citizenId;
        @NotNull(groups = {OnCreate.class, OnUpdate.class})
        private Role role;

        public UserDTO() {}

        public static UserDTO from(User entity){
            UserDTO dto = new UserDTO();
            dto.setId(entity.getId());
            dto.setEmail(entity.getEmail());
            dto.setTelephone(entity.getTelephone());
            dto.setFullname(entity.getFullname());
            dto.setCitizenId(entity.getCitizenId());
            dto.setRole(entity.getRole());
            return dto;
        }
    }

    @Getter
    @Setter
    public static class RequestLoginUser {
        @NotBlank()
        private String email;
        @NotBlank()
        private String password;

        public RequestLoginUser(){}
    }

    // -----------------------------------------------------------------------
    // POST requests
    // -----------------------------------------------------------------------

    @PostMapping(value = "/create")
    public ResponseEntity<?> createUser(HttpSession session, @Validated(OnCreate.class) @RequestBody UserDTO data){
        if(!DEV_MODE){
            Long userId = (Long)session.getAttribute("id");
            boolean authorized = userService.authorize(userId, Role.ADMINISTRATOR);
            if(!authorized) return ResponseEntity.status(401).body("User not authorized");
        }

        Long createdId = userService.createUser(data);
        return ResponseEntity.ok(createdId);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> login(HttpServletRequest request, @Validated @RequestBody RequestLoginUser data) {
        try {
            User user = userService.authenticate(data.getEmail(), data.getPassword());
            HttpSession session = request.getSession(true);
            session.setAttribute("id", user.getId());
            return ResponseEntity.ok("Login successful");
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // GET requests
    // -----------------------------------------------------------------------

    @GetMapping("/get/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session){
        Long id = (Long)session.getAttribute("id");
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserDTO.from(user));
    }

    @GetMapping("/get/list")
    public ResponseEntity<?> getUsers(HttpSession session){
        if(!DEV_MODE){
            Long userId = (Long) session.getAttribute("id");
            boolean authorized = userService.authorize(userId, Role.ADMINISTRATOR);
            if(!authorized) return ResponseEntity.status(401).body("User not authorized");
        }
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users.stream().map(user -> {
            return UserDTO.from(user);
        }).toList());
    }

    @GetMapping(value = "/logout")
    public ResponseEntity<?> logout(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        try {
            session.invalidate();
            return ResponseEntity.ok("Goodbye");
        }
        catch(IllegalStateException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Patch requests
    // -----------------------------------------------------------------------

    @PatchMapping("/update")
    public ResponseEntity<?> updateUser(HttpSession session, @Validated(OnUpdate.class) @RequestBody UserDTO data){
        Long id = (Long) session.getAttribute("id");
        boolean isAdmin = userService.authorize(id, Role.ADMINISTRATOR);

        if(!isAdmin && !data.getId().equals(id)){
            return ResponseEntity.status(401).body("Not authorized");
        }
    
        Long userId = userService.updateUser(data);
        return ResponseEntity.ok(userId);
    }

    // -----------------------------------------------------------------------

}