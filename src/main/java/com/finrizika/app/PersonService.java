package com.finrizika.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import org.springframework.stereotype.Service;
import com.finrizika.app.PersonController.CreateCreditApplicationDTO;
import com.finrizika.app.PersonController.CreateCreditDTO;
import com.finrizika.app.PersonController.CreateEmploymentDTO;
import com.finrizika.app.PersonController.ImportCreditDTO;
import com.finrizika.app.PersonController.ImportPaymentDTO;
import com.finrizika.app.PersonController.PersonDTO;
import com.finrizika.app.PersonController.UpdateCreditApplicationDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

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

    private boolean checkEmployment(Person person){
        long jobCount = person.getEmploymentHistory().stream().filter(employment -> employment.getEndDate() == null).count();
        if(jobCount > 0) return true;
        return false;
    }

    private Integer dtiScoring(Person person){
        List<Credit> creditHistory = person.getCreditHistory();
        BigDecimal totalDebt = creditHistory.stream().map(Credit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal currentSalary = person.getEmploymentHistory().stream().filter(employment -> employment.getEndDate() == null).map(Employment::getSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal dti = totalDebt.divide(currentSalary, 10, RoundingMode.HALF_UP);
        if(dti.compareTo(BigDecimal.valueOf(0.5)) > 0) return 0;
        else if(dti.compareTo(BigDecimal.valueOf(0.3)) < 0) return 30;
        else return 15;
    }

    private Integer latenessScoring(Person person){
        List<Credit> creditHistoryPast2Years = person.getCreditHistory().stream().filter(credit -> credit.getIssuedDate().plusYears(2).isAfter(LocalDate.now())).toList();
        List<Payment> allPayments = creditHistoryPast2Years.stream().flatMap(credit -> {
            return credit.getPayments().stream();
        }).toList();
        long latePaymentCount = allPayments.stream().filter(payment -> payment.getStatus() == PaymentStatus.LATE || payment.getStatus() == PaymentStatus.MISSED).count();
        if(latePaymentCount > 2) return 0;
        else if(latePaymentCount >= 1) return 20;
        else return 40;
    }

    private Integer salaryScoring(Person person){
        BigDecimal currentSalary = person.getEmploymentHistory().stream().filter(employment -> employment.getEndDate() == null).map(Employment::getSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
        if(currentSalary.compareTo(BigDecimal.valueOf(1000)) < 0) return 0;
        else if(currentSalary.compareTo(BigDecimal.valueOf(2000)) > 0) return 20;
        else return 10;
    }

    private Integer lengthScoring(Person person){
        List<Employment> currentJobs = person.getEmploymentHistory().stream().filter(employment -> employment.getEndDate() == null).toList();
        OptionalLong biggestLength = currentJobs.stream().mapToLong(job -> ChronoUnit.YEARS.between(job.getStartDate(), LocalDate.now())).max();
        if(biggestLength.getAsLong() < 2) return 15;
        else return 30;
    }

    public Integer calculateScore(Long personId){
        Person person = personRepository.findById(personId).orElseThrow(() -> new EntityNotFoundException("User not found."));

        if(!checkEmployment(person)) return 0;

        Integer score = 0;
        Integer dtiScore = dtiScoring(person);
        if(dtiScore == 0) return 0;
        score += dtiScore;
        score += latenessScoring(person);
        score += salaryScoring(person);
        score += lengthScoring(person);

        return score;
    }

    // ---------------------------------------------------------

    public Person getById(Long id){
        return personRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Person not found."));
    }

    public List<Person> getList(){
        return personRepository.findAll();
    }

    public List<Employment> getEmploymentList(Long personId){
        Person person = personRepository.findById(personId).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        return person.getEmploymentHistory();
    }

    public List<CreditApplication> getApplicationList(Long personId){
        Person person = personRepository.findById(personId).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        return person.getCreditApplicationHistory();
    }

    public List<Credit> getCreditsFromPerson(Long personId){
        Person person = personRepository.findById(personId).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        return person.getCreditHistory();
    }

    public List<Payment> getPaymentsFromCredit(Long creditId) {
        Credit credit = creditRepository.findById(creditId).orElseThrow(() -> new EntityNotFoundException("Credit not found."));
        return credit.getPayments();
    }

    // ---------------------------------------------------------
    
    public Long savePerson(PersonDTO dto){
        Person p = Person.from(dto);
        if (personRepository.existsByCitizenId(p.getCitizenId())) {
            return null;
        }
        Person saved = personRepository.save(p);
        return saved.getId();
    }

    public Long saveEmployment(CreateEmploymentDTO dto){
        Person person = personRepository.findById(dto.getPersonId()).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        Employment employment = Employment.from(dto);
        employment.setPerson(person);
        Employment saved = employmentRepository.save(employment);
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

    public Long importCredit(ImportCreditDTO dto){
        Person person = personRepository.findById(dto.getPersonId()).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        Credit credit = Credit.from(dto);
        credit.setIndividual(person);
        Credit saved = creditRepository.save(credit);
        return saved.getId();
    }

    public Long importPayment(ImportPaymentDTO dto){
        Credit credit = creditRepository.findById(dto.getCreditId()).orElseThrow(() -> new EntityNotFoundException("Credit not found."));
        Payment payment = Payment.from(dto);
        payment.setCredit(credit);
        Payment saved = paymentRepository.save(payment);
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

    public Long updateApplicationStatus(UpdateCreditApplicationDTO dto){
        CreditApplication application = creditApplicationRepository.findById(dto.getId()).orElseThrow(() -> new EntityNotFoundException("Credit application not found."));
        application.setStatus(dto.getStatus());
        CreditApplication saved = creditApplicationRepository.save(application);
        return saved.getId();
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

    public Long deleteCredit(Long creditId){
        Credit credit = creditRepository.findById(creditId).orElseThrow(() -> new EntityNotFoundException("Credit not found."));
        credit.softDelete();
        Credit deleted = creditRepository.save(credit);
        return deleted.getId();
    }

    public void deleteEmployment(Long employmentId) {
        Employment employment = employmentRepository.findById(employmentId).orElseThrow(() -> new EntityNotFoundException("Employment not found."));
        employmentRepository.delete(employment);
    }

}