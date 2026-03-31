package com.finrizika.app;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.finrizika.app.PersonController.PersonDTO;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

enum Sex{
    MALE,
    FEMALE,
    OTHER
}

@Getter
@Setter
@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"citizenId", "deleted"})
)
public class Person extends Individual{

    @Column(nullable = false)
    private String citizenId;

    @Column(nullable = false)
    private String fullname;

    @Column(nullable = false)
    private String telephone;

    @Column(nullable = true)
    private String email;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String zipcode;

    @Column(nullable = false)
    private LocalDate birthday;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Sex sex;

    @OneToMany(mappedBy = "person" , cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Employment> employmentHistory = new ArrayList<>();

    public Person() {}

    // -----------------------------------------------------------------
    // Factories
    // -----------------------------------------------------------------

    public static Person from(PersonDTO dto){
        Person person = new Person();
        person.setCitizenId(dto.getCitizenId());
        person.setFullname(dto.getFullname());
        person.setTelephone(dto.getTelephone());
        person.setEmail(dto.getEmail());
        person.setCountry(dto.getCountry());
        person.setRegion(dto.getRegion());
        person.setCity(dto.getCity());
        person.setZipcode(dto.getZipcode());
        person.setBirthday(dto.getBirthday());
        person.setSex(dto.getSex());
        return person;
    }

    // -----------------------------------------------------------------

}