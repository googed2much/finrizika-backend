package com.finrizika.app;

import java.time.LocalDate;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentScheduler {

    private final PaymentRepository paymentRepository;

    public PaymentScheduler(PaymentRepository paymentRepository){
        this.paymentRepository = paymentRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void checkMissedPayments() {
        List<Payment> overdue = paymentRepository.findByStatusAndDueDate(PaymentStatus.PENDING, LocalDate.now());
        overdue.forEach(p -> p.setStatus(PaymentStatus.MISSED));
        paymentRepository.saveAll(overdue);
    }
}
