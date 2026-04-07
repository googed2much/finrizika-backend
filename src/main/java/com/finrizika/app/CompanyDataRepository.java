package com.finrizika.app;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyDataRepository extends JpaRepository<CompanyData, Long> {
    Optional<CompanyData> findById(Long id);
    Optional<CompanyData> findByCompany_Id(Long id);
}
