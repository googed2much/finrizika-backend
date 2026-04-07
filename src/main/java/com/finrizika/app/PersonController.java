package com.finrizika.app;

import java.io.IOError;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.UrlResource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/physical")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService){
        this.personService = personService;
    }

    // ----------------------------------------------------------------------------------------------
    // CLASSES FOR REQUESTS AND RESPONSES (DTOs)
    // ----------------------------------------------------------------------------------------------

    private interface OnCreate {}
    private interface OnUpdate {}

    @Getter
    @Setter
    public static class PersonDTO {
        @NotNull(groups = {OnUpdate.class})
        private Long id;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String citizenId;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String fullname;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String telephone;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String email;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String country;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String region;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String city;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String zipcode;
        @NotNull(groups = {OnCreate.class, OnUpdate.class})
        private LocalDate birthday;
        @NotNull(groups = {OnCreate.class, OnUpdate.class})
        private Sex sex;

        public PersonDTO() {}

        public static PersonDTO from(Person entity) {
            PersonDTO personDTO = new PersonDTO();
            personDTO.setId(entity.getId());
            personDTO.setCitizenId(entity.getCitizenId());
            personDTO.setFullname(entity.getFullname());
            personDTO.setTelephone(entity.getTelephone());
            personDTO.setEmail(entity.getEmail());
            personDTO.setCountry(entity.getCountry());
            personDTO.setRegion(entity.getRegion());
            personDTO.setCity(entity.getCity());
            personDTO.setZipcode(entity.getZipcode());
            personDTO.setBirthday(entity.getBirthday());
            personDTO.setSex(entity.getSex());
            return personDTO;
        }
    }

    @Getter
    @Setter
    public static class SaveFileDTO{
        private Long personId;
        private MultipartFile file;

        public SaveFileDTO(){}
    }

    @Getter
    @Setter
    public static class CreateEmploymentDTO {
        @NotNull
        private Long personId;
        @NotNull
        private BigDecimal salary;
        @NotNull
        private BigDecimal post;
        @NotBlank
        private String employer;
        @NotBlank
        private String position;
        @NotNull
        private LocalDate startDate;
        private LocalDate endDate;
        @NotBlank
        private String workphone;

        public CreateEmploymentDTO(){}
    }

    @Getter
    @Setter
    public static class CreateCreditDTO {
        @NotNull
        private Long personId;
        @NotNull
        private BigDecimal amount;
        @NotNull
        private BigDecimal interestRate;
        @NotNull
        private Integer numberOfInstallments;

        public CreateCreditDTO(){}
    }

    @Getter
    @Setter
    public static class CreateCreditApplicationDTO {
        @NotNull
        private Long personId;
        @NotNull
        private BigDecimal requestedAmount;

        public CreateCreditApplicationDTO(){}
    }

    @Getter
    @Setter
    public static class UpdateCreditApplicationDTO {
        @NotNull
        private Long id;
        @NotNull
        private ApplicationStatus status;

        public UpdateCreditApplicationDTO(){}
    }

    @Getter
    @Setter
    public static class UpdatePaymentDTO {
        @NotNull
        private Long id;

        public UpdatePaymentDTO(){}
    }

    @Getter
    @Setter
    public static class ImportCreditDTO {
        @NotNull
        private Long personId;
        @NotNull
        private BigDecimal amount;
        @NotNull
        private BigDecimal interestRate;
        @NotNull
        private LocalDate issuedDate;
        @NotNull
        private LocalDate dueDate;
        @NotNull
        private CreditStatus status;
        @NotNull
        private CreditType type;
        private Long latePaymentCount;

        public ImportCreditDTO(){}
    }

    @Getter
    @Setter
    public static class ImportPaymentDTO {
        @NotNull
        private Long creditId;
        @NotNull
        private BigDecimal amount;
        private LocalDate paymentDate;
        @NotNull
        private LocalDate dueDate;
        @NotNull
        private PaymentStatus status;
    }

    @Getter
    @Setter
    public static class SendCreditDTO {
        private Long id;
        private BigDecimal amount;
        private BigDecimal interestRate;
        private LocalDate issuedDate;
        private LocalDate dueDate;
        private CreditStatus status;
        private CreditType type;
        private Long latePaymentCount;

        public SendCreditDTO(){}

        public static SendCreditDTO from(Credit entity){
            SendCreditDTO dto = new SendCreditDTO();
            dto.setId(entity.getId());
            dto.setAmount(entity.getAmount());
            dto.setInterestRate(entity.getInterestRate());
            dto.setIssuedDate(entity.getIssuedDate());
            dto.setDueDate(entity.getDueDate());
            dto.setStatus(entity.getStatus());
            dto.setType(entity.getType());
            dto.setLatePaymentCount(entity.getLatePaymentCount());
            return dto;
        }
    }

    @Getter
    @Setter
    public static class SendPaymentDTO {
        private Long id;
        private BigDecimal amount;
        private LocalDate paymentDate;
        private LocalDate dueDate;
        private PaymentStatus status;

        public SendPaymentDTO(){}

        public static SendPaymentDTO from(Payment entity){
            SendPaymentDTO dto = new SendPaymentDTO();
            dto.setId(entity.getId());
            dto.setAmount(entity.getAmount());
            dto.setPaymentDate(entity.getPaymentDate());
            dto.setDueDate(entity.getDueDate());
            dto.setStatus(entity.getStatus());
            return dto;
        }
    }

    @Getter
    @Setter
    public static class SendEmploymentDTO{
        private Long id;
        private BigDecimal salary;
        private BigDecimal post;
        private String employer;
        private String position;
        private LocalDate startDate;
        private LocalDate endDate;
        private String workphone;

        public SendEmploymentDTO(){}

        public static SendEmploymentDTO from(Employment entity){
            SendEmploymentDTO dto = new SendEmploymentDTO();
            dto.setId(entity.getId());
            dto.setSalary(entity.getSalary());
            dto.setPost(entity.getPost());
            dto.setEmployer(entity.getEmployer());
            dto.setPosition(entity.getPosition());
            dto.setStartDate(entity.getStartDate());
            dto.setEndDate(entity.getEndDate());
            dto.setWorkphone(entity.getWorkphone());
            return dto;
        }
    }

    @Getter
    @Setter
    public static class SendApplicationDTO{
        private Long id;
        private BigDecimal requestedAmount;
        private LocalDate appliedDate;
        private ApplicationStatus status;

        public SendApplicationDTO(){}

        public static SendApplicationDTO from(CreditApplication entity){
            SendApplicationDTO dto = new SendApplicationDTO();
            dto.setId(entity.getId());
            dto.setRequestedAmount(entity.getRequestedAmount());
            dto.setAppliedDate(entity.getAppliedDate());
            dto.setStatus(entity.getStatus());
            return dto;
        }
    }

    @Getter
    @Setter
    public static class SendDocumentDTO{

        private Long id;
        private String filename;
        private String originalName;
        public SendDocumentDTO(){}

        public static SendDocumentDTO from(Document document){
            SendDocumentDTO dto = new SendDocumentDTO();
            dto.setId(document.getId());
            dto.setFilename(document.getFilename());
            dto.setOriginalName(document.getOriginalName());
            return dto;
        }
    }

    // ----------------------------------------------------------------------------------------------
    // GET REQUESTS FOR GETTING DATA
    // ----------------------------------------------------------------------------------------------

    /**
     * Gets one person's data by ID.
     * @param id Person's id.
     * @return JSON of a single person's data. NotFound response if the person is not found.
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<?> getPersonById(@PathVariable long id){
        try{
            Person result = personService.getById(id);
            return ResponseEntity.ok(PersonDTO.from(result));
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/get/by/{id}")
    public ResponseEntity<?> getPersonById(@PathVariable String id){
        try{
            Person result = personService.getByCitizenId(id);
            return ResponseEntity.ok(PersonDTO.from(result));
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/get/pages")
    public ResponseEntity<?> getPageInfo(){
        try{
            Integer pageInfo = personService.getLastPageInfo();
            return ResponseEntity.ok(pageInfo);
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Gets a list of all people in the database.
     * @return JSON array of people's data.
     */
    @GetMapping("/get/list")
    public ResponseEntity<?> getList(){
        List<Person> result = personService.getList();
        return ResponseEntity.ok(result.stream().map(person -> {
            return PersonDTO.from(person);
        }).toList());
    }
    @GetMapping("/get/list/{page}")
    public ResponseEntity<?> getListPaged(@PathVariable Long page){
        List<Person> result = personService.getListPaged(page);
        return ResponseEntity.ok(result.stream().map(person -> {
            return PersonDTO.from(person);
        }).toList());
    }

    /**
     * Gets the employment list of the person.
     * @param id Person's id.
     * @return JSON array of jobs' data. NotFound response if the person is not found.
     */
    @GetMapping("/get/{id}/employment")
    public ResponseEntity<?> getPersonEmployment(@PathVariable Long id){
        List<Employment> result = personService.getEmploymentList(id);
        return ResponseEntity.ok(result.stream().map(employment -> {
            return SendEmploymentDTO.from(employment);
        }).toList());
    }

    /**
     * Gets all person's applications and their data.
     * @param id Person's id.
     * @return JSON array of applications' data. NotFound response if the person is not found.
     */
    @GetMapping("/get/{id}/applications")
    public ResponseEntity<?> getPersonApplications(@PathVariable Long id){
        List<CreditApplication> result = personService.getApplicationList(id);
        return ResponseEntity.ok(result.stream().map(application -> {
            return SendApplicationDTO.from(application);
        }).toList());
    }

    /**
     * Gets all person's credits.
     * @param id Person's id.
     * @return JSON array of credits' data. NotFound response if the person is not found.
     */
    @GetMapping("/get/{id}/credits")
    public ResponseEntity<?> getPersonCredits(@PathVariable Long id){
        try{
            List<Credit> creditHistory = personService.getCreditsFromPerson(id);
            return ResponseEntity.ok(creditHistory.stream().map(credit -> {
                return SendCreditDTO.from(credit);
            }).toList());
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/get/{id}/documents")
    public ResponseEntity<?> getDocumentList(@PathVariable Long id){
        try{
            List<Document> documents = personService.getDocumentList(id);
            return ResponseEntity.ok(documents.stream().map(document -> {
                return SendDocumentDTO.from(document);
            }).toList());
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/get/document/{id}")
    public ResponseEntity<?> getDocument(@PathVariable Long id){
        try{
            Document document = personService.getDocument(id);
            try{
                UrlResource resource = personService.retrieveDocument(document);
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(document.getContentType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getOriginalName() + "\"").body(resource);
            }
            catch(MalformedURLException e){
                return ResponseEntity.internalServerError().body(e.getMessage());
            }
            catch(IOError e){
                return ResponseEntity.internalServerError().body(e.getMessage());
            }
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Gets all payment data of the credit.
     * @param id Credit's id.
     * @return JSON array of payments' data. NotFound response if the person is not found.
     */
    @GetMapping("/get/credits/{id}/payments")
    public ResponseEntity<?> getCreditPayments(@PathVariable Long id){
        try{
            List<Payment> payments = personService.getPaymentsFromCredit(id);
            return ResponseEntity.ok(payments.stream().map(payment -> {
                return SendPaymentDTO.from(payment);
            }));
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Gets a person's score. Be sure to fill out all of their data, or else it will be just 0.
     * @param id Person's id.
     * @return Plain text of the person's score.
     */
    @GetMapping("/get/{id}/score")
    public ResponseEntity<?> getPersonScore(@PathVariable Long id){
        try{
            Integer score = personService.calculateScore(id);
            return ResponseEntity.ok(score);
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/get/{id}/scores")
    public ResponseEntity<?> getPersonScores(@PathVariable Long id){
        try{
            Map<String, Integer> scores = personService.calculateScores(id);
            return ResponseEntity.ok(scores);
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // POST REQUESTS FOR CREATING OR IMPORTING DATA
    // ----------------------------------------------------------------------------------------------------------------

    /**
     * Saves a person in the database.
     * @param data DTO of a person.
     * @return Plain text of the new person's ID. InternalServerError if something goes wrong.
     */
    @PostMapping("/save")
    public ResponseEntity<?> addPerson(@Validated(OnCreate.class) @RequestBody PersonDTO data){
        Long personId = personService.savePerson(data);
        if(personId == null) return ResponseEntity.internalServerError().body("Failed to save a person.");
        return ResponseEntity.ok(personId);
    }

    /**
     * Saves a single entry of employment for a person.
     * @param data DTO of a single employment entry.
     * @return Plain text of the newely created job's ID. InternalServerError if something goes wrong.
     */
    @PostMapping("/save/employment")
    public ResponseEntity<?> addEmployment(@Validated @RequestBody CreateEmploymentDTO data){
        Long employmentId = personService.saveEmployment(data);
        if(employmentId == null) return ResponseEntity.internalServerError().body("Failed to append employment.");
        return ResponseEntity.ok(employmentId);
    }

    /**
     * 
     * @return
     */
    @PostMapping(value="/upload/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addDocument(@ModelAttribute SaveFileDTO dto){
        if (dto.getFile() == null || dto.getFile().isEmpty()) {
            return ResponseEntity.badRequest().body("No file provided");
        }

        try{
            Long documentId = personService.saveDocument(dto.getPersonId(), dto.getFile());
            return ResponseEntity.ok(documentId);
        }
        catch(IOException e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     * Creates an application in the database for a person.
     * @param data DTO of an application.
     * @return Plain text of the newly created application's ID. InternalServerError if something goes wrong.
     */
    @PostMapping("/create/application")
    public ResponseEntity<?> createApplication(@Validated @RequestBody CreateCreditApplicationDTO data){
        Long creditApplicationId = personService.createCreditApplication(data);
        if(creditApplicationId == null) return ResponseEntity.internalServerError().body("Unable to create an application.");
        return ResponseEntity.ok(creditApplicationId);
    }
    
    /**
     * Creates a credit object for a person. Used for giving out credits. Will manually create new payments!!!
     * @param data DTO of a credit.
     * @return Plain text of newly created credit. InternalServerError if something goes wrong.
     */
    @PostMapping("/create/credit")
    public ResponseEntity<?> createCredit(@Validated @RequestBody CreateCreditDTO data){
        Long creditId = personService.createCredit(data);
        if(creditId == null) return ResponseEntity.internalServerError().body("Failed to create a credit.");
        return ResponseEntity.ok(creditId);
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     * Imports credit data.
     * @param data Fully filled DTO of a credit.
     * @return Plain text of the newly created credit ID. InternalServerError if something goes wrong.
     */
    @PostMapping("/import/credit")
    public ResponseEntity<?> importCredit(@Validated @RequestBody ImportCreditDTO data){
        Long creditId = personService.importCredit(data);
        if(creditId == null) return ResponseEntity.internalServerError().body("Failed to import credit.");
        return ResponseEntity.ok(creditId);
    }

    /**
     * Imports payment data.
     * @param data Fully filled DTO of a payment.
     * @return Plain text of the newly created payment ID. InternalServerError if something goes wrong.
     */
    @PostMapping("/import/payment")
    public ResponseEntity<?> importPayment(@Validated @RequestBody ImportPaymentDTO data){
        Long paymentId = personService.importPayment(data);
        if(paymentId == null) return ResponseEntity.internalServerError().body("Failed to import payment.");
        return ResponseEntity.ok(paymentId);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // PUT REQUESTS FOR UPDATING DATA
    // ----------------------------------------------------------------------------------------------------------------

    /**
     * Updates person's info.
     * @param data Fully filled DTO of the person. IDs must match someone in the database.
     * @return The person's ID. InternalServerError if something goes wrong.
     */
    @PatchMapping("/update")
    public ResponseEntity<?> updatePerson(@Validated(OnUpdate.class) @RequestBody PersonDTO data){
        Long personId = personService.updatePerson(data);
        if(personId == null) return ResponseEntity.internalServerError().body("Failed to update individual.");
        return ResponseEntity.ok(personId);
    }

    /**
     * Updates a credit application. Be sure to capitalize the status of it, as it is enum.
     * @param data DTO of the credit application.
     * @return The application's ID. InternalServerError if something goes wrong.
     */
    @PatchMapping("/update/application")
    public ResponseEntity<?> updateApplicationStatus(@Validated @RequestBody UpdateCreditApplicationDTO data){
        Long applicationId = personService.updateApplicationStatus(data);
        if(applicationId == null) return ResponseEntity.internalServerError().body("Failed to update application status.");
        return ResponseEntity.ok(applicationId);
    }

    /**
     * Makes a payment. The DTO only needs the payment's ID. It will automatically calculate if it's paid late or on time.
     * @param data DTO of the payment. Includes "id".
     * @return The payment's ID.
     */
    @PatchMapping("/update/payment")
    public ResponseEntity<?> updatePayment(@Validated @RequestBody UpdatePaymentDTO data){
        Long paymentId = personService.makePayment(data.getId());
        if(paymentId == null) return ResponseEntity.internalServerError().body("Failed to make payment.");
        return ResponseEntity.ok(paymentId);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // DELETE REQUESTS FOR (soft) DELETING DATA
    // ----------------------------------------------------------------------------------------------------------------

    /**
     * Soft deletes a person.
     * @param id Person's ID.
     * @return The ID of the deleted person. BadRequest if the person is not found. InternalServerError if something goes wrong.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePerson(@PathVariable Long id){
        try{
            Long deletedId = personService.deletePerson(id);
            if(id == null || deletedId != id) return ResponseEntity.internalServerError().body("Failed to delete individual.");
            return ResponseEntity.ok(deletedId);
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Soft deletes a credit for the person.
     * @param id Credit's ID.
     * @return The ID of the deleted credit. BadRequest if the credit is not found. InternalServerError if something goes wrong.
     */
    @DeleteMapping("/delete/credit/{id}")
    public ResponseEntity<?> deleteCredit(@PathVariable Long id){
        try{
            Long deletedId = personService.deleteCredit(id);
            if(deletedId == null || deletedId != id) return ResponseEntity.internalServerError().body("Failed to delete credit.");
            return ResponseEntity.ok(deletedId);
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Deletes an employment entry for the person.
     * @param id Employment's ID.
     * @return null if successful. BadRequest if the employment is not found. InternalServerError if something goes wrong.
     */
    @DeleteMapping("/delete/employment/{id}")
    public ResponseEntity<?> deleteEmployment(@PathVariable Long id){
        try{
            personService.deleteEmployment(id);
            return ResponseEntity.ok(null);
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        catch(OptimisticLockingFailureException e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
}