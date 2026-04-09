package com.finrizika.app;

import java.math.BigDecimal;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
)
public class CompanyData extends Individual{

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

   public CompanyData(Company company) { 
        this.company = company;

        this.shortTermAssets = BigDecimal.ZERO;
        this.inventory = BigDecimal.ZERO;
        this.shortTermLiabilities = BigDecimal.ZERO;

        this.cash = BigDecimal.ZERO;
        this.equity = BigDecimal.ZERO;
        this.totalAssets = BigDecimal.ZERO;

        this.netProfit = BigDecimal.ZERO;
        this.interest = BigDecimal.ZERO;
        this.taxes = BigDecimal.ZERO;

        this.financialLiabilities = BigDecimal.ZERO;

        this.depreciation = BigDecimal.ZERO;
        this.amortization = BigDecimal.ZERO;

        this.salesRevenueCurrent = BigDecimal.ZERO;
        this.salesRevenue1YearOld = BigDecimal.ZERO;
    }
    public CompanyData(){
        
    }

    // --------------------------------------------------------------------------
    // Factories
    // --------------------------------------------------------------------------

   
    // --------------------------------------------------------------------------

}
