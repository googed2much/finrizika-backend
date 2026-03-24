package com.finrizika.app;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhysicalIndividualRepository extends JpaRepository<PhysicalIndividual, Long> {
    Optional<PhysicalIndividual> findById(Long id);
    List<PhysicalIndividual> findByCreatedById(Long createdById);
}
