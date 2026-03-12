package com.finrizika.app;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

//fizinis asmuo isgalvoti atributai
@Data
@Entity
public class PhysicalIndividual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private double wage;
    private double debt;
    private double networth;
    private double expenses;
    private int age;
    private double score;

    public PhysicalIndividual() {}
    
}
