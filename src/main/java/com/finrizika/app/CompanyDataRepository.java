package com.finrizika.app;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyDataRepository extends JpaRepository<CompanyData, Long> {
    Optional<CompanyData> findById(long Id);

    List<CompanyData> findByCompanyCode(String companyCode);

}
