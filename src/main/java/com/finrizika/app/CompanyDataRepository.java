package com.finrizika.app;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyDataRepository extends JpaRepository<CompanyData, Long> {
    Optional<CompanyData> findByCompanyId(long companyId);
}
