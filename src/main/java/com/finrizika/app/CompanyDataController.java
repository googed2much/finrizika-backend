package com.finrizika.app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/company")
public class CompanyDataController {

    private final CompanyDataService companyDataService;

    public CompanyDataController(CompanyDataService companyDataService) {
        this.companyDataService = companyDataService;
    }

    // -----------------------------------------------------------------------
    // POST /api/company/data/create
    // -----------------------------------------------------------------------
    @PostMapping("/data/create")

    public ResponseEntity<?> createCompanyData(@RequestBody CompanyDataCreateDTO request) {
        int score = companyDataService.createCompanyData(request);
        return ResponseEntity.ok(score);
    }

}
