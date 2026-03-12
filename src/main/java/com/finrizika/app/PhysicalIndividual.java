package com.finrizika.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

//fizinis asmuo isgalvoti atributai
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getWage() {
        return wage;
    }

    public void setWage(double wage) {
        this.wage = wage;
    }

    public double getDebt() {
        return debt;
    }

    public void setDebt(double debt) {
        this.debt = debt;
    }

    public double getNetworth() {
        return networth;
    }

    public void setNetworth(double networth) {
        this.networth = networth;
    }

    public double getExpenses() {
        return expenses;
    }

    public void setExpenses(double expenses) {
        this.expenses = expenses;
    }

    public int getAge(){
        return age;
    } 
    public void setAge(int age){
        this.age = age;
    }

    public double getScore(){
        return score;
    } 
    public void setScore(double score){
        this.score = score;
    }
}
