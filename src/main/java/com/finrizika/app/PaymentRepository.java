package com.finrizika.app;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStatusAndDueDate(PaymentStatus status, LocalDate dueDate);
}
