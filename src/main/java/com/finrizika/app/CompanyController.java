package com.finrizika.app;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.io.IOError;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/juridical")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // -----------------------------------------------------------------------
    // DTOs
    // -----------------------------------------------------------------------

    private interface OnCreate {}
    private interface OnUpdate {}

    @Getter
    @Setter
    public static class CompanyDTO {
        @NotNull(groups = {OnUpdate.class})
        private Long id;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String companyId;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String name;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String ownerFullname;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String telephone;
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        private String email;
        public CompanyDTO(){}

        public static CompanyDTO from(Company entity){
            CompanyDTO dto = new CompanyDTO();
            dto.setCompanyId(entity.getCompanyId());
            dto.setName(entity.getName());
            dto.setOwnerFullname(entity.getOwnerFullname());
            dto.setTelephone(entity.getTelephone());
            dto.setEmail(entity.getEmail());
            dto.setId(entity.getId());
            return dto;
        }
    }
    @Getter
    @Setter
    public static class UpdateCompanyDataDTO {
        
        private Long companyId;

        private BigDecimal shortTermAssets;
        private BigDecimal inventory;
        private BigDecimal shortTermLiabilities;

        private BigDecimal equity;
        private BigDecimal totalAssets;
            
        private BigDecimal netProfit;
        private BigDecimal interest;
        private BigDecimal taxes;
            
        private BigDecimal financialLiabilities;
        private BigDecimal cash;
        private BigDecimal depreciation;
        private BigDecimal amortization;

        private BigDecimal salesRevenueCurrent;
        private BigDecimal salesRevenue1YearOld;
        public UpdateCompanyDataDTO(){}
        public static UpdateCompanyDataDTO from(CompanyData entity){
            UpdateCompanyDataDTO dto = new UpdateCompanyDataDTO();
            dto.setCompanyId(entity.getCompany().getId());

            dto.setShortTermAssets(entity.getShortTermAssets());
            dto.setInventory(entity.getInventory());
            dto.setShortTermLiabilities(entity.getShortTermLiabilities());

            dto.setEquity(entity.getEquity());
            dto.setTotalAssets(entity.getTotalAssets());
                
            dto.setNetProfit(entity.getNetProfit());
            dto.setInterest(entity.getInterest());
            dto.setTaxes(entity.getTaxes());
                
            dto.setFinancialLiabilities(entity.getFinancialLiabilities());
            dto.setCash(entity.getCash());
            dto.setDepreciation(entity.getDepreciation());
            dto.setAmortization(entity.getAmortization());

            dto.setSalesRevenueCurrent(entity.getSalesRevenueCurrent());
            dto.setSalesRevenue1YearOld(entity.getSalesRevenue1YearOld());
            return dto;
        } 
    }
      @Getter
    @Setter
    public static class SendCompanyDataDTO {
        private Long id;
        private BigDecimal shortTermAssets;
        private BigDecimal inventory;
        private BigDecimal shortTermLiabilities;

        private BigDecimal equity;
        private BigDecimal totalAssets;
            
        private BigDecimal netProfit;
        private BigDecimal interest;
        private BigDecimal taxes;
            
        private BigDecimal financialLiabilities;
        private BigDecimal cash;
        private BigDecimal depreciation;
        private BigDecimal amortization;

        private BigDecimal salesRevenueCurrent;
        private BigDecimal salesRevenue1YearOld;
        public SendCompanyDataDTO(){}
        public static SendCompanyDataDTO from(CompanyData entity){
            SendCompanyDataDTO dto = new SendCompanyDataDTO();
            dto.setId(entity.getId());

            dto.setShortTermAssets(entity.getShortTermAssets());
            dto.setInventory(entity.getInventory());
            dto.setShortTermLiabilities(entity.getShortTermLiabilities());

            dto.setEquity(entity.getEquity());
            dto.setTotalAssets(entity.getTotalAssets());
                
            dto.setNetProfit(entity.getNetProfit());
            dto.setInterest(entity.getInterest());
            dto.setTaxes(entity.getTaxes());
                
            dto.setFinancialLiabilities(entity.getFinancialLiabilities());
            dto.setCash(entity.getCash());
            dto.setDepreciation(entity.getDepreciation());
            dto.setAmortization(entity.getAmortization());

            dto.setSalesRevenueCurrent(entity.getSalesRevenueCurrent());
            dto.setSalesRevenue1YearOld(entity.getSalesRevenue1YearOld());
            return dto;
        } 
    }
    @Getter
    @Setter
    public static class SaveFileDTO{
        private Long companyId;
        private MultipartFile file;

        public SaveFileDTO(){}
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

    // -----------------------------------------------------------------------

    @GetMapping("/get")
    public ResponseEntity<?> getAllCompanies() {
        List<Company> results = companyService.getCompanyList();
        return ResponseEntity.ok(results.stream().map(company -> {
            return CompanyDTO.from(company);
        }));
    }
    @GetMapping("/get/list/{page}")
    public ResponseEntity<?> getAllCompaniesPaged(@PathVariable Long page) {
        List<Company> results = companyService.getListPaged(page);
        return ResponseEntity.ok(results.stream().map(company -> {
            return CompanyDTO.from(company);
        }));
    }
    
    @GetMapping("/get/{id}")
    public ResponseEntity<?> getCompany(@PathVariable Long id) {
        try{
            Company results = companyService.getCompany(id);
            return ResponseEntity.ok(CompanyDTO.from(results));
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/get/by/{id}")
    public ResponseEntity<?> getCompany(@PathVariable String id) {
        try{
            Company results = companyService.getCompanybyId(id);
            return ResponseEntity.ok(CompanyDTO.from(results));
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/get/data/{id}")
    public ResponseEntity<?> getCompanyData(@PathVariable Long id) {
        try{
            UpdateCompanyDataDTO results = companyService.getCompanyData(id);
            return ResponseEntity.ok(results);
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

   
    @GetMapping("/get/{id}/documents")
    public ResponseEntity<?> getDocumentList(@PathVariable Long id){
        try{
            List<Document> documents = companyService.getDocumentList(id);
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
            Document document = companyService.getDocument(id);
            try{
                UrlResource resource = companyService.retrieveDocument(document);
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
    @GetMapping("/get/{id}/scores")
    public ResponseEntity<?> getPersonScores(@PathVariable Long id){
        try{
            Map<String, BigDecimal> scores = companyService.calculateScores(id);
            return ResponseEntity.ok(scores);
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/get/pages")
    public ResponseEntity<?> getPageInfo(){
        try{
            Integer pageInfo = companyService.getLastPageInfo();
            return ResponseEntity.ok(pageInfo);
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }
    // -----------------------------------------------------------------------

    @PostMapping("/create")
    public ResponseEntity<?> createCompanyData(@Validated(OnCreate.class) @RequestBody CompanyDTO data) {
        Long id = companyService.createCompany(data);
        return ResponseEntity.ok(id);
    }

    @PostMapping(value="/upload/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addDocument(@ModelAttribute SaveFileDTO dto){
        if (dto.getFile() == null || dto.getFile().isEmpty()) {
            return ResponseEntity.badRequest().body("No file provided");
        }

        try{
            Long documentId = companyService.saveDocument(dto.getCompanyId(), dto.getFile());
            return ResponseEntity.ok(documentId);
        }
        catch(IOException e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    // ------------------------------------------------------------------------

    @PostMapping("/update")
    public ResponseEntity<?> updateCompany(@Validated(OnUpdate.class) @RequestBody CompanyDTO data){
        try{
            Long id = companyService.updateCompany(data);
            return ResponseEntity.ok(id);
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/update/data")
    public ResponseEntity<?> updateCompanyData(@Validated(OnUpdate.class) @RequestBody UpdateCompanyDataDTO data){
        try{
            Long id = companyService.updateCompanyData(data);
            return ResponseEntity.ok(id);
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }
    // ------------------------------------------------------------------------

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable Long id){
        try{
            Long deletedId = companyService.deleteCompany(id);
            return ResponseEntity.ok(deletedId);
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

}