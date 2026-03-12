package com.finrizika.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class CompanyData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double quickLiquidityRatio;

    @Column(nullable = false)
    private double equityRatio;

    @Column(nullable = false)
    private double interestCoverage;

    @Column(nullable = false)
    private double netDebtRatio;

    @Column(nullable = false)
    private double netProfitability;

    @Column(nullable = false)
    private double changeInSalesRevenue;

    @Column(nullable = false)
    private long companyId;

    public CompanyData() {
    }
    
}
