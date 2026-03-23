package com.finrizika.app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

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

    // -----------------------------------------------------------------------
    // GET /api/company/data
    // -----------------------------------------------------------------------
    @GetMapping("/data")
    public ResponseEntity<?> getAllCompanies() {
        List<CompanyDataResponseDTO> results = companyDataService.getAllCompanies();
        return ResponseEntity.ok(results);
    }

    // -----------------------------------------------------------------------
    // GET /api/company/data/{companyCode}
    // -----------------------------------------------------------------------
    @GetMapping("/data/{companyCode}")
    public ResponseEntity<?> getCompanyDataByCompanyCode(@PathVariable String companyCode) {
        List<CompanyDataResponseDTO> results = companyDataService.getByCompanyCode(companyCode);
        return ResponseEntity.ok(results);
    }

}
