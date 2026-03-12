package com.finrizika.app;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@RequestMapping("/api/physical")
@CrossOrigin(
    origins = "http://localhost:5173",
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class PhysIndividualController {

    private final PhysIndividualService service;

    public PhysIndividualController(PhysIndividualService service){
        this.service = service;
    }

    public static class RatingRequest{
        public double wage;
        public double debt;
        public double networth;
        public double expenses;
        public int age;
        public int id;
        public String name;
        public String telephone;
    }

    public static class RatingResponse{
        public double score;
        public RatingResponse(double score){
            this.score = score;
        }
    }

    @PostMapping("/calculate")
    public ResponseEntity<?> calculate(@RequestBody RatingRequest data){
        double score = service.saveAndCalculate(
            data.wage,
            data.debt,
            data.networth,
            data.expenses,
            data.age,
            data.id,
            data.name,
            data.telephone
        );
        return ResponseEntity.ok(new RatingResponse(score));
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable long id){
        Optional<PhysicalIndividual> result = service.findById(id);

        if(result.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result.get());
    }
}