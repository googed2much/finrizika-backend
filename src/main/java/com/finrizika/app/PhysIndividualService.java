package com.finrizika.app;

import org.springframework.stereotype.Service;
@Service
public class PhysIndividualService {

    private final PhysicalIndividualRepository repository;

    public PhysIndividualService(PhysicalIndividualRepository repository) {
        this.repository = repository;
    }

    //Fizinio asmens ivertinimas, isgalvoti daugikliai
    public double calculateScore(double wage, double debt, double networth, double expenses, int age){

        double score = 0;

        score += wage * 0.3;
        score -= debt * 0.2;
        score += networth * 0.2;
        score -= expenses * 0.2;
        score -= age * 0.06;

        return score;
    }

    public double saveAndCalculate(double wage, double debt, double networth, double expenses, int age,int id,String name,String telephone){

        double score = calculateScore(wage, debt, networth, expenses, age);

        PhysicalIndividual p = new PhysicalIndividual();
        p.setWage(wage);
        p.setDebt(debt);
        p.setNetworth(networth);
        p.setExpenses(expenses);
        p.setAge(age);
        p.setScore(score);
        p.setId(id);
        p.setName(name);
        p.setTelephone(telephone);
        repository.save(p);

        return score;
    }
    public Optional<PhysicalIndividual> findById(long id){
        return repository.findById(id);
    }
}
