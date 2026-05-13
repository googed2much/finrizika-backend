package com.finrizika.app;

import java.math.BigDecimal;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class CompanyData{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    private BigDecimal shortTermAssets;
    private BigDecimal inventory;
    private BigDecimal shortTermLiabilities;
    private BigDecimal equity;
    private BigDecimal totalAssets;
    private BigDecimal netProfit;
    private BigDecimal interest;
    private BigDecimal taxes;
    private BigDecimal financialLiabilities;
    private BigDecimal cash;
    private BigDecimal depreciation;
    private BigDecimal amortization;
    private BigDecimal salesRevenueCurrent;
    private BigDecimal salesRevenue1YearOld;
    
    public CompanyData(){}

    // --------------------------------------------------------------------------
    // Factories
    // --------------------------------------------------------------------------
    

    static public CompanyData empty(){
        CompanyData companyData = new CompanyData();
        companyData.setShortTermAssets(null);
        companyData.setInventory(null);
        companyData.setShortTermLiabilities(null);
        companyData.setEquity(null);
        companyData.setTotalAssets(null);
        companyData.setNetProfit(null);
        companyData.setTaxes(null);
        companyData.setFinancialLiabilities(null);
        companyData.setCash(null);
        companyData.setDepreciation(null);
        companyData.setAmortization(null);
        companyData.setSalesRevenueCurrent(null);
        companyData.setSalesRevenue1YearOld(null);
        return companyData;
    }

    // --------------------------------------------------------------------------

}
