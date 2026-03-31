package com.finrizika.app;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.finrizika.app.PersonController.CreateEmploymentDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Employment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal salary;

    @Column(nullable = false)
    private BigDecimal post; // 1 for full time, 0 for contract, in-between for part time

    @Column(nullable = false)
    private String employer;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate; // null if current

    @Column(nullable = false)
    private String workphone;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    public Employment(){}

    // --------------------------------------------------
    // Factories
    // --------------------------------------------------

    public static Employment from(CreateEmploymentDTO dto){
        Employment employment = new Employment();
        employment.setSalary(dto.getSalary());
        employment.setPost(dto.getPost());
        employment.setEmployer(dto.getEmployer());
        employment.setPosition(dto.getPosition());
        employment.setStartDate(dto.getStartDate());
        employment.setEndDate(dto.getEndDate());
        employment.setWorkphone(dto.getWorkphone());
        return employment;
    }

    // --------------------------------------------------
}
