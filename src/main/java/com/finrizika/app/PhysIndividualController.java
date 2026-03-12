package com.finrizika.app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/physicalCalc")
@CrossOrigin(origins = "http://localhost:5173")
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
    }

    public static class RatingResponse{
        public double score;
        public RatingResponse(double score){
            this.score = score;
        }
    }

    @PostMapping
    public ResponseEntity<?> calculate(@RequestBody RatingRequest data){
        double score = service.saveAndCalculate(
            data.wage,
            data.debt,
            data.networth,
            data.expenses,
            data.age
        );
        return ResponseEntity.ok(new RatingResponse(score));
    }
}