package com.finrizika.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.finrizika.app.PersonController.CreateCreditApplicationDTO;
import com.finrizika.app.PersonController.CreateCreditDTO;
import com.finrizika.app.PersonController.EmploymentDTO;
import com.finrizika.app.PersonController.PersonDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class PersonService {

    private final PersonRepository personRepository;
    private final EmploymentRepository employmentRepository;
    private final CreditRepository creditRepository;
    private final PaymentRepository paymentRepository;
    private final CreditApplicationRepository creditApplicationRepository;

    public PersonService(PersonRepository personRepository, EmploymentRepository employmentRepository, CreditRepository creditRepository, PaymentRepository paymentRepository, CreditApplicationRepository creditApplicationRepository) {
        this.personRepository = personRepository;
        this.employmentRepository = employmentRepository;
        this.creditRepository = creditRepository;
        this.paymentRepository = paymentRepository;
        this.creditApplicationRepository = creditApplicationRepository;
    }

    // ---------------------------------------------------------

    private void generatePaymentSchedule(Credit credit, Integer numberOfInstallments){
        LocalDate due = credit.getIssuedDate();

        BigDecimal totalAmount = credit.getAmount().multiply(BigDecimal.ONE.add(credit.getInterestRate())); // Flat sum = Amount * (1.00+intr.rate)

        BigDecimal installment = totalAmount.divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.HALF_UP); // each month's installment.

        List<Payment> schedule = new ArrayList<>();
        for(int i=0; i<numberOfInstallments; i++){
            Payment payment = new Payment();
            if(i == numberOfInstallments - 1) { // last one gets the few cents missing
                BigDecimal paid = installment.multiply(BigDecimal.valueOf(numberOfInstallments - 1));
                payment.setAmount(totalAmount.subtract(paid));
            }
            else {
                payment.setAmount(installment);
            }
            payment.setPaymentDate(null);
            payment.setDueDate(due.plusMonths(i + 1));
            payment.setStatus(PaymentStatus.PENDING);
            payment.setCredit(credit);
            schedule.add(payment);
        }
        paymentRepository.saveAll(schedule);
    }

    private double calculateScore(){
        // TODO: CREATE A FORMULA
        return 0.f;
    }

    // ---------------------------------------------------------

    public Person getById(Long id){
        return personRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Person not found."));
    }
    public List<Person> getList(){
        return personRepository.findAll();
    }

    // ---------------------------------------------------------

    public Long savePerson(PersonDTO dto){
        Person p = Person.from(dto);
        Person saved = personRepository.save(p);
        return saved.getId();
    }

    public Long saveEmployment(EmploymentDTO dto){
        Person person = personRepository.findById(dto.getPersonId()).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        Employment employment = Employment.from(dto);
        employment.setPerson(person);
        Employment saved = employmentRepository.save(employment);
        return saved.getId();
    }

    public Long createCredit(CreateCreditDTO dto){
        Person person = personRepository.findById(dto.getPersonId()).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        Credit credit = Credit.from(dto);
        credit.setIndividual(person);
        credit.setDueDate(credit.getIssuedDate().plusMonths(dto.getNumberOfInstallments()));
        credit.setStatus(CreditStatus.ACTIVE);
        credit.setIssuedDate(LocalDate.now());
        Credit saved = creditRepository.save(credit);
        generatePaymentSchedule(saved, dto.getNumberOfInstallments());
        return saved.getId();
    }

    public Long createCreditApplication(CreateCreditApplicationDTO dto){
        Person person = personRepository.findById(dto.getPersonId()).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        CreditApplication creditApplication = CreditApplication.from(dto);
        creditApplication.setAppliedDate(LocalDate.now());
        creditApplication.setStatus(ApplicationStatus.PENDING);
        creditApplication.setIndividual(person);
        CreditApplication saved = creditApplicationRepository.save(creditApplication);
        return saved.getId();
    }

    // ---------------------------------------------------------

    public Long updatePerson(PersonDTO dto){
        Person person = personRepository.findById(dto.getId()).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        person.setCitizenId(dto.getCitizenId());
        person.setFullname(dto.getFullname());
        person.setTelephone(dto.getTelephone());
        person.setEmail(dto.getEmail());
        person.setCountry(dto.getCountry());
        person.setRegion(dto.getRegion());
        person.setCity(dto.getCity());
        person.setZipcode(dto.getZipcode());
        person.setBirthday(dto.getBirthday());
        person.setSex(dto.getSex());
        Person updated = personRepository.save(person);
        return updated.getId();
    }

    public Long makePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new EntityNotFoundException("Payment not found."));
        LocalDate today = LocalDate.now();
        payment.setPaymentDate(today);
        payment.setStatus(today.isAfter(payment.getDueDate()) ? PaymentStatus.LATE : PaymentStatus.PAID);
        Payment saved = paymentRepository.save(payment);
        return saved.getId();
    }

    // ---------------------------------------------------------

    public Long deletePerson(Long id){
        Person person = personRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        person.softDelete();
        Person deleted = personRepository.save(person);
        return deleted.getId();
    }
}
