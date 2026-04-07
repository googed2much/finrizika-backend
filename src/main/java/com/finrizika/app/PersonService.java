package com.finrizika.app;

import java.io.IOError;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import com.finrizika.app.PersonController.CreateCreditApplicationDTO;
import com.finrizika.app.PersonController.CreateCreditDTO;
import com.finrizika.app.PersonController.CreateEmploymentDTO;
import com.finrizika.app.PersonController.ImportCreditDTO;
import com.finrizika.app.PersonController.ImportPaymentDTO;
import com.finrizika.app.PersonController.PersonDTO;
import com.finrizika.app.PersonController.UpdateCreditApplicationDTO;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class PersonService {

    private Integer NUM_ELEMENTS_PER_PAGE=10;
    private final PersonRepository personRepository;
    private final EmploymentRepository employmentRepository;
    private final DocumentRepository documentRepository;
    private final CreditRepository creditRepository;
    private final PaymentRepository paymentRepository;
    private final CreditApplicationRepository creditApplicationRepository;

    public PersonService(PersonRepository personRepository, EmploymentRepository employmentRepository, DocumentRepository documentRepository, CreditRepository creditRepository, PaymentRepository paymentRepository, CreditApplicationRepository creditApplicationRepository) {
        this.personRepository = personRepository;
        this.employmentRepository = employmentRepository;
        this.documentRepository = documentRepository;
        this.creditRepository = creditRepository;
        this.paymentRepository = paymentRepository;
        this.creditApplicationRepository = creditApplicationRepository;
    }

    @Value("${app.document.upload-directory}")
    private String storagePath;
    private Path documentStorageLocation;
    @PostConstruct
    public void initializeStorage() throws IOException{
        this.documentStorageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(this.documentStorageLocation);
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

    // ----------------------------------------------------------------------------------------------------------------------------------
    // TODO: IMTI VIDURKI PER 6 MENESIUS
    // TODO: STAZAS 

    private boolean checkEmployment(Person person){
        long jobCount = person.getEmploymentHistory().stream().filter(employment -> employment.getEndDate() == null).count();
        if(jobCount > 0) return true;
        return false;
    }

    private Integer dtiScoring(Person person){
        List<Credit> creditHistory = person.getCreditHistory();
        List<Credit> activeCredits = creditHistory.stream().filter(credit -> credit.getStatus() == CreditStatus.ACTIVE).toList();
        BigDecimal monthlyPayment = activeCredits.stream().map(credit -> {
            List<Payment> payments = credit.getPayments();
            if(payments.size() != 0) return payments.get(0).getAmount();
            return BigDecimal.ZERO;
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal currentSalary = person.getEmploymentHistory().stream().filter(employment -> employment.getEndDate() == null).map(Employment::getSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal dti = monthlyPayment.divide(currentSalary, 10, RoundingMode.HALF_UP);
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
     private Integer latenessCreditScoring(Person person){
        List<Credit> creditHistoryPast2Years = person.getCreditHistory().stream().filter(credit -> credit.getIssuedDate().plusYears(2).isAfter(LocalDate.now())).toList();
        long latePaymentCount = creditHistoryPast2Years.stream().mapToLong(credit -> credit.getLatePaymentCount()==null? 0: credit.getLatePaymentCount()).sum();
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
        score += latenessCreditScoring(person);
        score += salaryScoring(person);
        score += lengthScoring(person);

        return score;
    }
    public Map<String, Integer> calculateScores(Long personId){
        Person person = personRepository.findById(personId).orElseThrow(() -> new EntityNotFoundException("User not found."));

        Map<String, Integer> scores = new HashMap<>();

        if(!checkEmployment(person)) {
            scores.put("totalScore", 0);
            return scores;
        }

        int dtiScore = dtiScoring(person);
        if(dtiScore == 0){
            scores.put("totalScore", 0);
            scores.put("dtiScore", 0);
            return scores;
        }

        int lateness = latenessCreditScoring(person);
        int salary = salaryScoring(person);
        int length = lengthScoring(person);

        int total = dtiScore + lateness + salary + length;

        scores.put("dtiScore", dtiScore);
        scores.put("latenessScore", lateness);
        scores.put("salaryScore", salary);
        scores.put("lengthScore", length);
        scores.put("totalScore", total);

        return scores;
    }
    // ----------------------------------------------------------------------------------------------------------------------------------

    public Person getById(Long personId){
        return personRepository.findById(personId).orElseThrow(() -> new EntityNotFoundException("Person not found."));
    }
    public Person getByCitizenId(String personId){
        return personRepository.findByCitizenId(personId).orElseThrow(() -> new EntityNotFoundException("Person not found."));
    }
    public Integer getLastPageInfo(){
        List<Person> people = personRepository.findAll();
        int lastPage = (int)Math.ceil(people.size()/NUM_ELEMENTS_PER_PAGE);
        return lastPage;
    }

    public List<Person> getList(){
        return personRepository.findAll();
    }
    public List<Person> getListPaged(Long page){
        int numPerPage = 10;
        List<Person> people =personRepository.findAll();
        List<Person> pagedList = new ArrayList<Person>();
        int pageStart = numPerPage*page.intValue();
        int pageEnd = Math.min(people.size(),pageStart+numPerPage);
        for(int i = pageStart;i<pageEnd;i++){
            pagedList.add(people.get(i));
        } 
        return pagedList;
    }

    public List<Employment> getEmploymentList(Long personId){
        Person person = personRepository.findById(personId).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        return person.getEmploymentHistory();
    }

    public List<Document> getDocumentList(Long personId) throws EntityNotFoundException{
        Person person = personRepository.findById(personId).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        return person.getDocuments();
    }

    public Document getDocument(Long documentId) throws EntityNotFoundException{
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new EntityNotFoundException());
        return document;
    }

    public UrlResource retrieveDocument(Document document) throws MalformedURLException, IOError{
        Path filePath = this.documentStorageLocation.resolve(document.getFilename()).normalize();
        System.out.println("Looking for file: " + filePath);
        UrlResource resource = new UrlResource(filePath.toUri());
        
        return resource;
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

    // ----------------------------------------------------------------------------------------------------------------------------------
    
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

    public Long saveDocument(Long personId, MultipartFile file) throws IOException, EntityNotFoundException{
        Person person = personRepository.findById(personId).orElseThrow(() -> new EntityNotFoundException("Person not found."));

        String filename = storeDocument(file);
        String originalName = file.getOriginalFilename();
        Document document = new Document();
        document.setFilename(filename);
        document.setContentType(file.getContentType());
        document.setIndividual(person);
        document.setOriginalName(originalName);
        Document saved = documentRepository.save(document);
        return saved.getId();
    }   

    private String storeDocument(MultipartFile file) throws IOException{
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID() + "_" + filename;
        Path targetLocation = this.documentStorageLocation.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
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
        if(dto.getNumberOfInstallments() < 12) credit.setType(CreditType.SHORT_TERM);
        else credit.setType(CreditType.LONG_TERM);
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