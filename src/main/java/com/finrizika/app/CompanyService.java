package com.finrizika.app;

import java.io.IOError;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.finrizika.app.CompanyController.CompanyDTO;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final DocumentRepository documentRepository;

    public CompanyService(CompanyRepository companyRepository, DocumentRepository documentRepository) {
        this.companyRepository = companyRepository;
        this.documentRepository = documentRepository;
    }

    @Value("${app.document.upload-directory}")
    private String storagePath;
    private Path documentStorageLocation;
    @PostConstruct
    public void initializeStorage() throws IOException{
        this.documentStorageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(this.documentStorageLocation);
    }

    // --------------------------------------------------------------------------------------------------------------


    public double calculateQuickLiquidityRatio(double shortTermAssets, double inventory, double shortTermLiabilities) {
        return (shortTermAssets - inventory) / shortTermLiabilities;
    }

    public double calculateEquityRatio(double equity, double totalAssets) {
        return equity / totalAssets;
    }

    public double calculateEBIT(double netProfit, double interest, double taxes) {
        return netProfit + interest + taxes;
    }

    public double calculateInterestCoverage(double ebit, double interestExpenses) {
        return ebit / interestExpenses;
    }

    public double calculateNetDebtRatio(double financialLiabilities, double cash, double ebit, double depreciation,
            double amortization) {
        return (financialLiabilities - cash) / (ebit + depreciation + amortization);
    }

    public double calculateNetProfitability(double netProfit, double salesRevenue) {
        return netProfit / salesRevenue;
    }

    public int calculateScore(
            double quickLiquidityRatio,
            double equityRatio,
            double interestCoverage,
            double netDebtRatio,
            double netProfitability,
            double changeInSalesRevenue) {
        double points = 0;

        // Quick Liquidity Ratio
        if (quickLiquidityRatio < 0.8) {
        } else if (quickLiquidityRatio < 1) {
            points += 50;
        } else if (quickLiquidityRatio < 1.2) {
            points += 100;
        } else if (quickLiquidityRatio < 1.5) {
            points += 140;
        } else {
            points += 166.67;
        }

        // Equity Ratio
        if (equityRatio < 0.2) {
        } else if (equityRatio < 0.3) {
            points += 50;
        } else if (equityRatio < 0.4) {
            points += 100;
        } else if (equityRatio < 0.5) {
            points += 140;
        } else {
            points += 166.67;
        }

        // Interest Coverage
        if (interestCoverage < 1) {
        } else if (interestCoverage < 2) {
            points += 50;
        } else if (interestCoverage < 3) {
            points += 100;
        } else if (interestCoverage < 5) {
            points += 140;
        } else {
            points += 166.67;
        }

        // Net Debt Ratio
        if (netDebtRatio > 5) {
        } else if (netDebtRatio > 4) {
            points += 50;
        } else if (netDebtRatio > 3) {
            points += 100;
        } else if (netDebtRatio > 2) {
            points += 140;
        } else {
            points += 166.67;
        }

        // Net Profitability
        if (netProfitability < 0) {
        } else if (netProfitability < 5) {
            points += 50;
        } else if (netProfitability < 10) {
            points += 100;
        } else if (netProfitability < 15) {
            points += 140;
        } else {
            points += 166.67;
        }

        // Change in Sales Revenue
        if (changeInSalesRevenue < -10) {
        } else if (changeInSalesRevenue < 0) {
            points += 50;
        } else if (changeInSalesRevenue < 5) {
            points += 100;
        } else if (changeInSalesRevenue < 15) {
            points += 140;
        } else {
            points += 166.67;
        }

        if (points > 1000) {
            points = 1000;
        }

        return Math.min(10, (int) Math.floor((1000 - points) / 100) + 1);
    }

    // --------------------------------------------------------------------------------------------------------------

    public Company getCompany(Long id) throws EntityNotFoundException{
        Company company = companyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Company not found"));
        return company;
    }

    public List<Company> getCompanyList() {
        List<Company> companies = companyRepository.findAll();
        return companies;
    }

    public List<Document> getDocumentList(Long companyId) throws EntityNotFoundException{
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new EntityNotFoundException("Company not found."));
        return company.getDocuments();
    }

    public Document getDocument(Long documentId) throws EntityNotFoundException{
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new EntityNotFoundException());
        return document;
    }

    public UrlResource retrieveDocument(Document document) throws MalformedURLException, IOError{
        Path filePath = this.documentStorageLocation.resolve(document.getFilename()).normalize();
        UrlResource resource = new UrlResource(filePath.toUri());
        return resource;
    }

    // --------------------------------------------------------------------------------------------------------------

    public Long createCompany(CompanyDTO dto) {
        Company company = Company.from(dto);
        Company saved = companyRepository.save(company);
        return saved.getId();
    }

    public Long saveDocument(Long companyId, MultipartFile file) throws IOException, EntityNotFoundException{
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new EntityNotFoundException("Company not found."));

        String filename = storeDocument(file);
        Document document = new Document();
        document.setFilename(filename);
        document.setContentType(file.getContentType());
        document.setIndividual(company);
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

    // --------------------------------------------------------------------------------------------------------------

    public Long updateCompany(CompanyDTO dto) throws EntityNotFoundException{
        Company company = companyRepository.findById(dto.getId()).orElseThrow(() -> new EntityNotFoundException("Company not found"));
        company.setCompanyId(dto.getCompanyId());
        company.setName(dto.getName());
        company.setOwnerFullname(dto.getOwnerFullname());
        company.setTelephone(dto.getTelephone());
        company.setEmail(dto.getEmail());
        Company saved = companyRepository.save(company);
        return saved.getId();
    }
    
    // --------------------------------------------------------------------------------------------------------------

    public Long deleteCompany(Long id) throws EntityNotFoundException{
        Company company = companyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Company not found"));
        company.softDelete();
        Company saved = companyRepository.save(company);
        return saved.getId();
    }

}