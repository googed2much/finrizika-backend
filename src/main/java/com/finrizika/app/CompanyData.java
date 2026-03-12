package com.finrizika.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

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

    // Getters and setters...

    public Long getId(){
        return this.id;
    }

    public void setId(Long id){
        this.id = id;
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

    public long getCompanyId() {
        return this.companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }
}
