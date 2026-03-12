package com.finrizika.app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

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
    public static class RequestCreateCompanyInfo {

        private double quickLiquidityRatio;
        private double equityRatio;
        private double interestCoverage;
        private double netDebtRatio;
        private double netProfitability;
        private double changeInSalesRevenue;

        public RequestCreateCompanyInfo() {
        }

        public double getQuickLiquidityRatio() {
            return quickLiquidityRatio;
        }

        public void setQuickLiquidityRatio(double quickLiquidityRatio) {
            this.quickLiquidityRatio = quickLiquidityRatio;
        }

        public double getEquityRatio() {
            return equityRatio;
        }

        public void setEquityRatio(double equityRatio) {
            this.equityRatio = equityRatio;
        }

        public double getInterestCoverage() {
            return interestCoverage;
        }

        public void setInterestCoverage(double interestCoverage) {
            this.interestCoverage = interestCoverage;
        }

        public double getNetDebtRatio() {
            return netDebtRatio;
        }

        public void setNetDebtRatio(double netDebtRatio) {
            this.netDebtRatio = netDebtRatio;
        }

        public double getNetProfitability() {
            return netProfitability;
        }

        public void setNetProfitability(double netProfitability) {
            this.netProfitability = netProfitability;
        }

        public double getChangeInSalesRevenue() {
            return changeInSalesRevenue;
        }

        public void setChangeInSalesRevenue(double changeInSalesRevenue) {
            this.changeInSalesRevenue = changeInSalesRevenue;
        }
    }

    // -----------------------------------------------------------------------
    // POST /api/company-info/create
    // -----------------------------------------------------------------------
    @PostMapping("/data/create")
    public ResponseEntity<?> createCompanyInfo(HttpServletRequest request, @RequestBody RequestCreateCompanyInfo data) {
        companyDataService.createCompanyData(data.getQuickLiquidityRatio(), data.getEquityRatio(), data.getInterestCoverage(), data.getNetDebtRatio(), data.getNetProfitability(), data.getChangeInSalesRevenue());
        return ResponseEntity.ok("Company created successfully");
    }

}
