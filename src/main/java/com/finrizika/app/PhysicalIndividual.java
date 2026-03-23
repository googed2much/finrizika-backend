package com.finrizika.app;

import java.util.Date;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

enum Sex{
    MALE,
    FEMALE,
    OTHER
}

enum HomeStatus{
    NONE,
    RENTING,
    MORTGAGE,
    OWNS
}

//fizinis asmuo isgalvoti atributai
@Data
@Entity
public class PhysicalIndividual {

    @Id
    private long id;
    private String fullname;
    private String telephone;
    private String country;
    private String region;
    private String city;
    private int zipcode;
    private Date birhtday;
    private Sex sex;
    private HomeStatus homeStatus;
    private long createdById;

    public PhysicalIndividual() {}

}
