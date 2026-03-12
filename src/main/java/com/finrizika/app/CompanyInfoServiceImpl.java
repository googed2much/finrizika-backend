package com.finrizika.app;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class CompanyInfoServiceImpl implements CompanyInfoService {

    private final CompanyInfoRepository repository;

    public CompanyInfoServiceImpl(CompanyInfoRepository repository) {
        this.repository = repository;
    }

    @Override
    public CompanyInfo createCompanyInfo(
            double quickLiquidityRatio,
            double equityRatio,
            double interestCoverage,
            double netDebtRatio,
            double netProfitability,
            double changeInSalesRevenue) {
        CompanyInfo info = new CompanyInfo();
        info.setQuickLiquidityRatio(quickLiquidityRatio);
        info.setEquityRatio(equityRatio);
        info.setInterestCoverage(interestCoverage);
        info.setNetDebtRatio(netDebtRatio);
        info.setNetProfitability(netProfitability);
        info.setChangeInSalesRevenue(changeInSalesRevenue);

        return repository.save(info);
    }

    @Override
    public Optional<CompanyInfo> getCompanyInfo(long id) {
        return repository.findById(id);
    }
}
