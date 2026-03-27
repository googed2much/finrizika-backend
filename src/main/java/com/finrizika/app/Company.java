package com.finrizika.app;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Company {

    @Id
    private Long code;

    private String name;
    private String owner;
    private String telephone;
    private String email;
    private Long createdById;

    public Company() { }

}
