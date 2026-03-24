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
    OWNER
}

@Data
@Entity
public class PhysicalIndividual {

    @Id
    private long id;
    private String fullname;
    private String telephone;
    private String email;
    private String country;
    private String region;
    private String city;
    private String zipcode;
    private Date birthday;
    private Sex sex;
    private HomeStatus homeStatus;
    private long createdById;

    public PhysicalIndividual() {}

}
