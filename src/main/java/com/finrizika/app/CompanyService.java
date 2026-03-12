package com.finrizika.app;

import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CompanyService{

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public void createCompany(double quickLiquidityRatio, double equityRatio, double interestCoverage, double netDebtRatio, double netProfitability, double changeInSalesRevenue) {
        Company company = new Company();
        company.setQuickLiquidityRatio(quickLiquidityRatio);
        company.setEquityRatio(equityRatio);
        company.setInterestCoverage(interestCoverage);
        company.setNetDebtRatio(netDebtRatio);
        company.setNetProfitability(netProfitability);
        company.setChangeInSalesRevenue(changeInSalesRevenue);

        companyRepository.save(company);
    }

    public Optional<Company> getCompanyById(long id) {
        return companyRepository.findById(id);
    }
}