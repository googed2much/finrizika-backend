package com.finrizika.app;

import lombok.Data;

@Data
public class CompanyDataCreateDTO {
    private Double shortTermAssets;
    private Double inventory;
    private Double shortTermLiabilities;
    private Double equity;
    private Double totalAssets;
    private Double netProfit;
    private Double interest;
    private Double taxes;
    private Double interestExpenses;
    private Double depreciation;
    private Double amortization;
    private Double financialLiabilities;
    private Double cash;
    private Double salesRevenue;
    private Double changeInSalesRevenue;
    private String companyCode;
    private String name;
    private String phoneNumber;
}
