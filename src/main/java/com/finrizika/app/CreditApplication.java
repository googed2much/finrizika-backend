package com.finrizika.app;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.finrizika.app.PersonController.CreateCreditApplicationDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

enum ApplicationStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Data
@Entity
public class CreditApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private BigDecimal requestedAmount;
    private LocalDate appliedDate;
    private ApplicationStatus status;

    @ManyToOne
    @JoinColumn(name = "individual_id")
    private Individual individual;

    public CreditApplication(){}

    // --------------------------------------------------
    // Factories
    // --------------------------------------------------

    public static CreditApplication from(CreateCreditApplicationDTO dto){
        CreditApplication creditApplication = new CreditApplication();
        creditApplication.setRequestedAmount(dto.getRequestedAmount());
        return creditApplication;
    }

    // --------------------------------------------------

}
