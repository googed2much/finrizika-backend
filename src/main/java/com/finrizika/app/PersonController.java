package com.finrizika.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
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
            
            return personDTO;
        }
    }

    @Getter
    @Setter
    public static class EmploymentDTO {
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

        public EmploymentDTO(){}
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
        private LocalDate issuedDate;
        @NotNull
        private CreditType type;
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
    public static class UpdatePaymentDTO {
        @NotNull
        private Long id;

        public UpdatePaymentDTO(){}
    }

    // ----------------------------------------------------------------------------------------------
    // GET REQUESTS FOR GETTING DATA
    // ----------------------------------------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<?> getPersonById(@PathVariable Long id){
        Person result = personService.getById(id);
        return ResponseEntity.ok(PersonDTO.from(result));
    }

    @GetMapping("/list")
    public ResponseEntity<?> getList(){
        List<Person> result = personService.getList();
        return ResponseEntity.ok(result.stream().map(person -> {
            return PersonDTO.from(person);
        }));
    }

    // ----------------------------------------------------------------------------------------------------------------
    // POST REQUESTS FOR SAVING DATA
    // ----------------------------------------------------------------------------------------------------------------
    // For creating things, like credits (NOT USED FOR IMPORTING THINGS!!!)

    @PostMapping("/create/credit")
    public ResponseEntity<?> createCredit(@Validated @RequestBody CreateCreditDTO data){
        Long creditId = personService.createCredit(data);
        if(creditId == null) return ResponseEntity.internalServerError().body("Failed to create a credit.");
        return ResponseEntity.ok(creditId);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // For saving things (USED FOR IMPORTING DATA FROM OTHER SOURCES)

    @PostMapping("/save")
    public ResponseEntity<?> savePerson(@Validated(OnCreate.class) @RequestBody PersonDTO data){
        Long personId = personService.savePerson(data);
        if(personId == null) return ResponseEntity.internalServerError().body("Failed to save a person.");
        return ResponseEntity.ok(personId);
    }

    @PostMapping("/save/employment")
    public ResponseEntity<?> addEmployment(@Validated(OnCreate.class) @RequestBody EmploymentDTO data){
        Long employmentId = personService.saveEmployment(data);
        if(employmentId == null) return ResponseEntity.internalServerError().body("Failed to append employment.");
        return ResponseEntity.ok(employmentId);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // PUT REQUESTS FOR UPDATING DATA
    // ----------------------------------------------------------------------------------------------------------------

    @PutMapping("/update")
    public ResponseEntity<?> updatePerson(@Validated(OnUpdate.class) @RequestBody PersonDTO data){
        Long personId = personService.updatePerson(data);
        if(personId == null) return ResponseEntity.internalServerError().body("Failed to update individual.");
        return ResponseEntity.ok(personId);
    }

    @PutMapping("/update/payment")
    public ResponseEntity<?> updatePayment(@Validated @RequestBody UpdatePaymentDTO data){
        Long paymentId = personService.makePayment(data.getId());
        if(paymentId == null) return ResponseEntity.internalServerError().body("Failed to make payment.");
        return ResponseEntity.ok(paymentId);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // DELETE REQUESTS FOR (soft) DELETING DATA
    // ----------------------------------------------------------------------------------------------------------------

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePerson(@PathVariable Long id){
        Long personId = personService.deletePerson(id);
        if(personId == null) return ResponseEntity.internalServerError().body("Failed to update individual.");
        return ResponseEntity.ok(personId);
    }

    // ----------------------------------------------------------------------------------------------------------------
}