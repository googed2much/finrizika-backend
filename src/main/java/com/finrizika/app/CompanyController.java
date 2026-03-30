package com.finrizika.app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/juridical")
public class CompanyController {

    private final CompanyService companyDataService;

    public CompanyController(CompanyService companyDataService) {
        this.companyDataService = companyDataService;
    }

    // -----------------------------------------------------------------------
    // POST /api/company/create
    // -----------------------------------------------------------------------
    @Data
    public static class CompanyDataCreateDTO {
        @NotNull()
        private Long code;
        @NotBlank()
        private String name;
        @NotBlank()
        private String owner;
        @NotBlank()
        private String telephone;
        @NotBlank()
        private String email;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCompanyData(HttpServletRequest request, @Valid @RequestBody CompanyDataCreateDTO data) {
        Long code = data.getCode();
        String name = data.getName();
        String owner = data.getOwner();
        String telephone = data.getTelephone();
        String email = data.getEmail();
        Long createdById = (Long) request.getSession().getAttribute("id");
        companyDataService.createCompany(code, name, owner, telephone, email, createdById);
        return ResponseEntity.ok("Created successfully");
    }

    // -----------------------------------------------------------------------
    // GET /api/company/get
    // -----------------------------------------------------------------------
    @Data
    public static class CompanyDTO {
        private Long code;
        private String name;
        private String owner;
        private String telephone;
        private String email;

        CompanyDTO() {}
        CompanyDTO(Company company) {
            this.code = company.getCode();
            this.name = company.getName();
            this.owner = company.getOwner();
            this.telephone = company.getTelephone();
            this.email = company.getEmail();
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getAllCompanies() {
        List<Company> results = companyDataService.getAllCompanies();
        return ResponseEntity.ok(results.stream().map(CompanyDTO::new).collect(Collectors.toList()));
    }
    
    @GetMapping("/get/{code}")
    public ResponseEntity<?> getCompanyByCompanyCode(@PathVariable Long code) {
        List<Company> results = companyDataService.getByCompanyCode(code);
        return ResponseEntity.ok(results.stream().map(CompanyDTO::new).collect(Collectors.toList()));
    }

}
