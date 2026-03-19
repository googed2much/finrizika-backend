package com.finrizika.app;

import java.util.Optional;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/physical")
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
    @GetMapping("/list")
    public ResponseEntity<?> getPhysicalList(){
        List<PhysicalIndividual> result = service.getList();

        if(result.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
    @PostMapping("/save/{id}")
    public ResponseEntity<?> saveToPortfolioById(@PathVariable long id){
        boolean saved = service.saveToPortfolio(id);
        if(!saved){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}