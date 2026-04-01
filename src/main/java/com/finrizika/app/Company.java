package com.finrizika.app;

import com.finrizika.app.CompanyController.CompanyDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"companyId", "deleted"})
)
public class Company extends Individual{

    @Column(nullable = false, unique = true)
    private String companyId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String ownerFullname;
    @Column(nullable = false)
    private String telephone;
    @Column(nullable = false)
    private String email;

    public Company() { }

    // --------------------------------------------------------------------------
    // Factories
    // --------------------------------------------------------------------------

    public static Company from(CompanyDTO dto){
        Company company = new Company();
        company.setCompanyId(dto.getCompanyId());
        company.setName(dto.getName());
        company.setOwnerFullname(dto.getOwnerFullname());
        company.setTelephone(dto.getTelephone());
        company.setEmail(dto.getEmail());
        return company;
    }

    // --------------------------------------------------------------------------

}
