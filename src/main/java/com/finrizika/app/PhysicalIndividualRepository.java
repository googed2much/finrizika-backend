package com.finrizika.app;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhysicalIndividualRepository extends JpaRepository<PhysicalIndividual, Long> {
    Optional<PhysicalIndividual> findById(Long id);
}
