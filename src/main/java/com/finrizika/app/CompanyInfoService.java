package com.finrizika.app;

import java.util.Optional;

public interface CompanyInfoService {
    CompanyInfo createCompanyInfo(
            double quickLiquidityRatio,
            double equityRatio,
            double interestCoverage,
            double netDebtRatio,
            double netProfitability,
            double changeInSalesRevenue);

    Optional<CompanyInfo> getCompanyInfo(long id);
}
