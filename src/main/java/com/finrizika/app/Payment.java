package com.finrizika.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.finrizika.app.PersonController.ImportPaymentDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

enum PaymentStatus{
    PENDING,
    PAID,
    LATE,
    MISSED
}

@Getter
@Setter
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    private LocalDate paymentDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @ManyToOne
    @JoinColumn(name = "credit_id")
    private Credit credit;

    public Payment(){}

    // ------------------------------------------------
    // Factories
    // ------------------------------------------------

    public static Payment from(ImportPaymentDTO dto){
        Payment payment = new Payment();
        payment.setAmount(dto.getAmount());
        payment.setPaymentDate(dto.getPaymentDate());
        payment.setDueDate(dto.getDueDate());
        payment.setStatus(dto.getStatus());
        return payment;
    }

    // ------------------------------------------------

}
