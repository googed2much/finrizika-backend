package com.finrizika.app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;

@RestController
@RequestMapping("/api/company")
public class CompanyController {

    private final CompanyDataService companyDataService;

    public CompanyController(CompanyDataService companyDataService) {
        this.companyDataService = companyDataService;
    }

    // -----------------------------------------------------------------------
    // DTO for creating CompanyInfo
    // -----------------------------------------------------------------------
    @Data
    public static class RequestCreateCompanyInfo {
        private double quickLiquidityRatio;
        private double equityRatio;
        private double interestCoverage;
        private double netDebtRatio;
        private double netProfitability;
        private double changeInSalesRevenue;
        private Long companyId;

        public RequestCreateCompanyInfo() {
        }
    }

    // -----------------------------------------------------------------------
    // POST /api/company-info/create
    // -----------------------------------------------------------------------
    @PostMapping("/data/create")
    public ResponseEntity<?> createCompanyInfo(HttpServletRequest request, @RequestBody RequestCreateCompanyInfo data) {
        companyDataService.createCompanyData(data.getQuickLiquidityRatio(), data.getEquityRatio(), data.getInterestCoverage(), data.getNetDebtRatio(), data.getNetProfitability(), data.getChangeInSalesRevenue(), data.getCompanyId());
        return ResponseEntity.ok("Company created successfully");
    }
    
}
