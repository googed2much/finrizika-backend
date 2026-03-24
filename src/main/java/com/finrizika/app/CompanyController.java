package com.finrizika.app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.finrizika.app.UserController.UserDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/company")
public class CompanyController {

    private final CompanyService companyDataService;

    public CompanyController(CompanyService companyDataService) {
        this.companyDataService = companyDataService;
    }

    // -----------------------------------------------------------------------
    // POST /api/company/create
    // -----------------------------------------------------------------------
    @Data
    public class CompanyDataCreateDTO {
        @jakarta.validation.constraints.NotNull()
        private Long code;
        @jakarta.validation.constraints.NotBlank()
        private String owner;
        @jakarta.validation.constraints.NotBlank()
        private String telephone;
        @jakarta.validation.constraints.NotBlank()
        private String email;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCompanyData(HttpServletRequest request, @Valid @RequestBody CompanyDataCreateDTO data) {
        Long code = data.getCode();
        String owner = data.getOwner();
        String telephone = data.getTelephone();
        String email = data.getEmail();
        Long createdById = (Long) request.getSession().getAttribute("id");
        companyDataService.createCompany(code, owner, telephone, email, createdById);
        return ResponseEntity.ok("Created successfully");
    }

    // -----------------------------------------------------------------------
    // GET /api/company/get
    // -----------------------------------------------------------------------
    @Data
    public class CompanyDTO {
        private Long code;
        private String owner;
        private String telephone;
        private String email;

        CompanyDTO() {}
        CompanyDTO(Company company) {
            this.code = company.getCode();
            this.owner = company.getOwner();
            this.telephone = company.getTelephone();
            this.email = company.getTelephone();
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getAllCompanies() {
        List<Company> results = companyDataService.getAllCompanies();
        return ResponseEntity.ok(results.stream().map(CompanyDTO::new).collect(Collectors.toList()));
    }
    
    @GetMapping("/get/{code}")
    public ResponseEntity<?> getCompanyByCompanyCode(@NotBlank @Size(min = 1) @PathVariable Long code) {
        List<Company> results = companyDataService.getByCompanyCode(code);
        return ResponseEntity.ok(results.stream().map(CompanyDTO::new).collect(Collectors.toList()));
    }

}
