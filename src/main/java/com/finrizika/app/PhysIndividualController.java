package com.finrizika.app;

import java.util.Date;
import java.util.Optional;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/physical")
public class PhysIndividualController {

    private final PhysIndividualService physicalService;
    private final UserService userService;

    public PhysIndividualController(PhysIndividualService physicalService, UserService userService){
        this.userService = userService;
        this.physicalService = physicalService;
    }

    @Data
    private static class PhysIndividualDTO{
        private long id;
        private String fullname;
        private String telephone;
        private String email;
        private String country;
        private String region;
        private String city;
        private String zipcode;
        private Date birthday;
        private Sex sex;
        private HomeStatus homeStatus;
        private String createdBy;

        public PhysIndividualDTO(PhysicalIndividual p){
            this.id = p.getId();
            this.fullname = p.getFullname();
            this.telephone = p.getTelephone();
            this.email = p.getEmail();
            this.country = p.getCountry();
            this.region = p.getRegion();
            this.city = p.getCity();
            this.zipcode = p.getZipcode();
            this.birthday = p.getBirthday();
            this.sex = p.getSex();
            this.homeStatus = p.getHomeStatus();
            this.createdBy = null;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable long id){
        Optional<PhysicalIndividual> result = physicalService.getById(id);
        if(result.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        PhysIndividualDTO dataToSend = new PhysIndividualDTO(result.get());
        Optional<User> creatorResult = userService.getUserById(result.get().getCreatedById());
        if(!creatorResult.isEmpty()) dataToSend.setCreatedBy(creatorResult.get().getFullname());
        return ResponseEntity.ok(dataToSend);
    }
    @GetMapping("/list")
    public ResponseEntity<?> getPhysicalList(){
        List<PhysicalIndividual> result = physicalService.getList();
        if(result.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
    @GetMapping("/mylist")
    public ResponseEntity<?> getMyPhysicalList(HttpServletRequest request){
        Long userId = (Long) request.getSession().getAttribute("id");
        List<PhysicalIndividual> result = physicalService.getListByCreator(userId);

        if(result.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    // ----------------------------------------------------------------------------------------------------------------
    @Data
    private static class SaveProfileRequest{
        @NotNull()
        private long id;
        @NotBlank()
        private String fullname;
        @NotNull()
        private String telephone;
        @NotNull()
        private String email;
        @NotNull()
        private String country;
        @NotNull()
        private String region;
        @NotNull()
        private String city;
        @NotNull()
        private String zipcode;
        @NotNull()
        private Date birthday;
        @NotNull()
        private Sex sex;
        @NotNull()
        private HomeStatus homeStatus;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveToPortfolio(HttpServletRequest request, @Valid @RequestBody SaveProfileRequest data){
        long id = data.getId();
        String fullname = data.getFullname();
        String telephone = data.getTelephone();
        String email = data.getEmail();
        String country = data.getCountry();
        String region = data.getRegion();
        String city = data.getCity();
        String zipcode = data.getZipcode();
        Date birthday = data.getBirthday();
        Sex sex = data.getSex();
        HomeStatus homeStatus = data.getHomeStatus();
        long createdById = (long) request.getSession().getAttribute("id");

        physicalService.saveProfile(id, fullname, telephone, email, country, region, city, zipcode, birthday, sex, homeStatus, createdById);

        return ResponseEntity.ok("Succesfully saved");
    }

    @PutMapping("/update")
    public ResponseEntity<?> updatePhysical(@Valid @RequestBody SaveProfileRequest data){
        long id = data.getId();
        String fullname = data.getFullname();
        String telephone = data.getTelephone();
        String email = data.getEmail();
        String country = data.getCountry();
        String region = data.getRegion();
        String city = data.getCity();
        String zipcode = data.getZipcode();
        Date birthday = data.getBirthday();
        Sex sex = data.getSex();
        HomeStatus homeStatus = data.getHomeStatus();

        System.out.println(fullname);

        physicalService.updateProfile(id, fullname, telephone, email, country, region, city, zipcode, birthday, sex, homeStatus);

        return ResponseEntity.ok("Succesfully updated");
    }

    @DeleteMapping("/delete/{:id}")
    public ResponseEntity<?> deletePhysical(HttpServletRequest request){



        return ResponseEntity.ok("Succesfully deleted");
    }
    // ----------------------------------------------------------------------------------------------------------------
}