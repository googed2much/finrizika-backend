package com.finrizika.app;

import java.util.Date;
import java.util.Optional;
import java.util.List;
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

    public void saveProfile(long id, String fullname, String telephone, String email, String country, String region, String city, String zipcode, Date birthday, Sex sex, HomeStatus homeStatus, long createById){
        PhysicalIndividual p = new PhysicalIndividual();
        p.setId(id);
        p.setFullname(fullname);
        p.setTelephone(telephone);
        p.setEmail(email);
        p.setCountry(country);
        p.setRegion(region);
        p.setCity(city);
        p.setZipcode(zipcode);
        p.setBirthday(birthday);
        p.setSex(sex);
        p.setHomeStatus(homeStatus);
        p.setCreatedById(createById);
        repository.save(p);
    }

    public Optional<PhysicalIndividual> findById(long id){
        return repository.findById(id);
    }
    public List<PhysicalIndividual> getList(){
        return repository.findAll();
    }
    public List<PhysicalIndividual> getListByCreator(Long creatorId){
        return repository.findByCreatedById(creatorId);
    }
}
