package com.finrizika.app;

import java.util.Date;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/physical")
public class PhysIndividualController {

    private final PhysIndividualService physicalService;

    public PhysIndividualController(PhysIndividualService physicalService){
        this.physicalService = physicalService;
    }

    @Data
    private static class RatingRequest{
        private double wage;
        private double debt;
        private double networth;
        private double expenses;
        private int age;

        public RatingRequest(){}
    }

    @PostMapping("/calculate")
    public ResponseEntity<?> calculate(@RequestBody RatingRequest data){
        double score = physicalService.calculateScore(
            data.wage,
            data.debt,
            data.networth,
            data.expenses,
            data.age
        );
        return ResponseEntity.ok(score);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable long id){
        Optional<PhysicalIndividual> result = physicalService.findById(id);

        if(result.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result.get());
    }

    // ----------------------------------------------------------------------------------------------------------------
    @Data
    private static class SaveProfileRequest{
        @jakarta.validation.constraints.NotNull()
        private long id;
        @jakarta.validation.constraints.NotBlank()
        private String fullname;
        @jakarta.validation.constraints.NotNull()
        private String telephone;
        @jakarta.validation.constraints.NotNull()
        private String country;
        @jakarta.validation.constraints.NotNull()
        private String region;
        @jakarta.validation.constraints.NotNull()
        private String city;
        @jakarta.validation.constraints.NotNull()
        private int zipcode;
        @jakarta.validation.constraints.NotNull()
        private Date birhtday;
        @jakarta.validation.constraints.NotNull()
        private Sex sex;
        @jakarta.validation.constraints.NotNull()
        private HomeStatus homeStatus;

        public SaveProfileRequest () {}
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveToPortfolio(HttpServletRequest request, @Valid @RequestBody SaveProfileRequest data){
        long id = data.getId();
        String fullname = data.getFullname();
        String telephone = data.getTelephone();
        String country = data.getCountry();
        String region = data.getRegion();
        String city = data.getCity();
        int zipcode = data.getZipcode();
        Date birthday = data.getBirhtday();
        Sex sex = data.getSex();
        HomeStatus homeStatus = data.getHomeStatus();
        long createdById = (long) request.getSession().getAttribute("id");

        physicalService.saveProfile(id, fullname, telephone, country, region, city, zipcode, birthday, sex, homeStatus, createdById);

        return ResponseEntity.ok(null);
    }
    // ----------------------------------------------------------------------------------------------------------------
}