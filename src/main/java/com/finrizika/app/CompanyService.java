package com.finrizika.app;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> getByCompanyCode(Long companyCode) {
        List<Company> matches = companyRepository.findByCode(companyCode);
        return matches;
    }

    public List<Company> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        return companies;
    }

    public void createCompany(Long code, String owner, String telephone, String email, Long createdById) {
        Company company = new Company();
        company.setCode(code);
        company.setOwner(owner);
        company.setTelephone(telephone);
        company.setEmail(email);
        company.setCreatedById(createdById);
        companyRepository.save(company);
    }

    public double calculateQuickLiquidityRatio(double shortTermAssets, double inventory, double shortTermLiabilities) {
        return (shortTermAssets - inventory) / shortTermLiabilities;
    }

    public double calculateEquityRatio(double equity, double totalAssets) {
        return equity / totalAssets;
    }

    public double calculateEBIT(double netProfit, double interest, double taxes) {
        return netProfit + interest + taxes;
    }

    public double calculateInterestCoverage(double ebit, double interestExpenses) {
        return ebit / interestExpenses;
    }

    public double calculateNetDebtRatio(double financialLiabilities, double cash, double ebit, double depreciation,
            double amortization) {
        return (financialLiabilities - cash) / (ebit + depreciation + amortization);
    }

    public double calculateNetProfitability(double netProfit, double salesRevenue) {
        return netProfit / salesRevenue;
    }

    public int calculateScore(
            double quickLiquidityRatio,
            double equityRatio,
            double interestCoverage,
            double netDebtRatio,
            double netProfitability,
            double changeInSalesRevenue) {
        double points = 0;

        // Quick Liquidity Ratio
        if (quickLiquidityRatio < 0.8) {
        } else if (quickLiquidityRatio < 1) {
            points += 50;
        } else if (quickLiquidityRatio < 1.2) {
            points += 100;
        } else if (quickLiquidityRatio < 1.5) {
            points += 140;
        } else {
            points += 166.67;
        }

        // Equity Ratio
        if (equityRatio < 0.2) {
        } else if (equityRatio < 0.3) {
            points += 50;
        } else if (equityRatio < 0.4) {
            points += 100;
        } else if (equityRatio < 0.5) {
            points += 140;
        } else {
            points += 166.67;
        }

        // Interest Coverage
        if (interestCoverage < 1) {
        } else if (interestCoverage < 2) {
            points += 50;
        } else if (interestCoverage < 3) {
            points += 100;
        } else if (interestCoverage < 5) {
            points += 140;
        } else {
            points += 166.67;
        }

        // Net Debt Ratio
        if (netDebtRatio > 5) {
        } else if (netDebtRatio > 4) {
            points += 50;
        } else if (netDebtRatio > 3) {
            points += 100;
        } else if (netDebtRatio > 2) {
            points += 140;
        } else {
            points += 166.67;
        }

        // Net Profitability
        if (netProfitability < 0) {
        } else if (netProfitability < 5) {
            points += 50;
        } else if (netProfitability < 10) {
            points += 100;
        } else if (netProfitability < 15) {
            points += 140;
        } else {
            points += 166.67;
        }

        // Change in Sales Revenue
        if (changeInSalesRevenue < -10) {
        } else if (changeInSalesRevenue < 0) {
            points += 50;
        } else if (changeInSalesRevenue < 5) {
            points += 100;
        } else if (changeInSalesRevenue < 15) {
            points += 140;
        } else {
            points += 166.67;
        }

        if (points > 1000) {
            points = 1000;
        }

        return Math.min(10, (int) Math.floor((1000 - points) / 100) + 1);
    }

}