package com.finrizika.app;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import java.util.Optional;
import jakarta.persistence.Column;
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
    @Column(nullable = false)
    private boolean inPortfolio = false;

    public PhysicalIndividual() {}

}
