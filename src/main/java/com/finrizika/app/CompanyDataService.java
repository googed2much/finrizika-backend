package com.finrizika.app;

import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CompanyDataService{

    private final CompanyDataRepository companyDataRepository;

    public CompanyDataService(CompanyDataRepository companyDataRepository) {
        this.companyDataRepository = companyDataRepository;
    }

    public void createCompanyData(double quickLiquidityRatio, double equityRatio, double interestCoverage, double netDebtRatio, double netProfitability, double changeInSalesRevenue) {
        CompanyData companyData = new CompanyData();
        companyData.setQuickLiquidityRatio(quickLiquidityRatio);
        companyData.setEquityRatio(equityRatio);
        companyData.setInterestCoverage(interestCoverage);
        companyData.setNetDebtRatio(netDebtRatio);
        companyData.setNetProfitability(netProfitability);
        companyData.setChangeInSalesRevenue(changeInSalesRevenue);

        companyDataRepository.save(companyData);
    }

    public Optional<CompanyData> getCompanyById(long companyId) {
        return companyDataRepository.findByCompanyId(companyId);
    }
}