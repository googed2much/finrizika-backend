package com.finrizika.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.SQLRestriction;
import com.finrizika.app.PersonController.CreateCreditDTO;
import com.finrizika.app.PersonController.ImportCreditDTO;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

enum CreditStatus{
    ACTIVE,
    PAID,
    DEFAULTED
}

enum CreditType{
    SHORT_TERM,
    LONG_TERM
}

@Getter
@Setter
@Entity
@SQLRestriction("deleted = false")
public class Credit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private LocalDate issuedDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CreditStatus status;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CreditType type;

    @ManyToOne
    @JoinColumn(name = "individual_id")
    private Individual individual;

    @OneToMany(mappedBy = "credit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<Payment>();

    private boolean deleted = false;
    private LocalDate deletedAt;
    public void softDelete(){
        this.deleted = true;
        this.deletedAt = LocalDate.now();
    }

    public Credit(){}

    // ---------------------------------------------------
    // Factories
    // ---------------------------------------------------

    public static Credit from(CreateCreditDTO dto){
        Credit credit = new Credit();
        credit.setAmount(dto.getAmount());
        credit.setIssuedDate(dto.getIssuedDate());
        credit.setInterestRate(dto.getInterestRate());
        credit.setType(dto.getType());
        return credit;
    }

    public static Credit from(ImportCreditDTO dto){
        Credit credit = new Credit();
        credit.setAmount(dto.getAmount());
        credit.setInterestRate(dto.getInterestRate());
        credit.setIssuedDate(dto.getIssuedDate());
        credit.setDueDate(dto.getDueDate());
        credit.setStatus(dto.getStatus());
        credit.setType(dto.getType());
        return credit;
    }

    // ---------------------------------------------------

}
