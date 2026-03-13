package com.finrizika.app;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

//fizinis asmuo isgalvoti atributai
@Data
@Entity
public class PhysicalIndividual {

    @Id
    private long id;
    private double wage;
    private double debt;
    private double networth;
    private double expenses;
    private int age;
    private double score;

    private String name;
    private String telephone;

    public PhysicalIndividual() {}

}
