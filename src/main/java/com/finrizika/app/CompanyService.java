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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.finrizika.app.CompanyController.CompanyDTO;
import com.finrizika.app.CompanyController.UpdateCompanyDataDTO;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

@Service
public class CompanyService {

    private Integer NUM_ELEMENTS_PER_PAGE=10;
    private final CompanyRepository companyRepository;
    private final DocumentRepository documentRepository;
    private final CompanyDataRepository companyDataRepo;

    public CompanyService(CompanyRepository companyRepository, DocumentRepository documentRepository,CompanyDataRepository companyDataRepo) {
        this.companyRepository = companyRepository;
        this.documentRepository = documentRepository;
        this.companyDataRepo = companyDataRepo;
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


    public BigDecimal calculateQuickLiquidityRatio(BigDecimal shortTermAssets, BigDecimal inventory, BigDecimal shortTermLiabilities) {
        return (shortTermAssets.subtract(inventory)).divide(shortTermLiabilities, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateEquityRatio(BigDecimal equity, BigDecimal totalAssets) {
        return equity.divide(totalAssets,RoundingMode.HALF_UP);
    }

    public BigDecimal calculateEBIT(BigDecimal netProfit, BigDecimal interest, BigDecimal taxes) {
        return netProfit.add(taxes).add(interest);
    }

    public BigDecimal calculateInterestCoverage(BigDecimal ebit, BigDecimal interestExpenses) {
        return ebit.divide(interestExpenses,RoundingMode.HALF_UP);
    }

    public BigDecimal calculateNetDebtRatio(BigDecimal financialLiabilities, BigDecimal cash, BigDecimal ebit, BigDecimal depreciation,
            BigDecimal amortization) {
        return (financialLiabilities.subtract(cash)).divide((ebit.add(depreciation).add(amortization)),RoundingMode.HALF_UP);
    }

    public BigDecimal calculateNetProfitability(BigDecimal netProfit, BigDecimal salesRevenue) {
        return netProfit.divide(salesRevenue,RoundingMode.HALF_UP);
    }
    public BigDecimal calculateChangeInSalesRevenue(BigDecimal yearOldRev, BigDecimal currentRev) {
        return (currentRev.subtract(yearOldRev)).divide(yearOldRev,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }
    public Map<String,BigDecimal> calculateScores(Long id) {
        CompanyData data = companyDataRepo.findByCompany_Id(id).orElseThrow(() -> new EntityNotFoundException("User not found."));
        Map<String,BigDecimal> map = new HashMap<>();
        double points = 0;
        BigDecimal equityRatio = calculateEquityRatio(data.getEquity(), data.getTotalAssets());
        BigDecimal ebit = calculateEBIT(data.getNetProfit(), data.getInterest(), data.getTaxes());
        BigDecimal interestCoverage= calculateInterestCoverage(ebit, data.getInterest());
        BigDecimal netDebtRatio = calculateNetDebtRatio(data.getFinancialLiabilities(), data.getCash(), ebit, data.getDepreciation(), data.getAmortization());
        BigDecimal netProfitability = calculateNetProfitability(data.getNetProfit(), data.getSalesRevenueCurrent()).multiply(BigDecimal.valueOf(100));
        BigDecimal changeInSalesRevenue = calculateChangeInSalesRevenue(data.getSalesRevenue1YearOld(), data.getSalesRevenueCurrent());
        BigDecimal quickLiquidityRatio =  calculateQuickLiquidityRatio(data.getShortTermAssets(),data.getInventory(),data.getShortTermLiabilities());
        // Quick Liquidity Ratio
        if (quickLiquidityRatio.compareTo(BigDecimal.valueOf(0.8))<0) {
        } else if (quickLiquidityRatio.compareTo(BigDecimal.valueOf(1)) < 0) {
            points += 50;
        } else if (quickLiquidityRatio.compareTo(BigDecimal.valueOf(1.2)) < 0) {
            points += 100;
        } else if (quickLiquidityRatio.compareTo(BigDecimal.valueOf(1.5)) < 0) {
            points += 140;
        } else {
            points += 166.67;
        }

        // Equity Ratio
        if (equityRatio.compareTo(BigDecimal.valueOf(0.2)) < 0) {
        } else if (equityRatio.compareTo(BigDecimal.valueOf(0.3)) < 0) {
            points += 50;
        } else if (equityRatio.compareTo(BigDecimal.valueOf(0.4)) < 0) {
            points += 100;
        } else if (equityRatio.compareTo(BigDecimal.valueOf(0.5)) < 0) {
            points += 140;
        } else {
            points += 166.67;
        }

        // Interest Coverage
        if (interestCoverage.compareTo(BigDecimal.valueOf(1)) < 0) {
        } else if (interestCoverage.compareTo(BigDecimal.valueOf(2)) <0) {
            points += 50;
        } else if (interestCoverage.compareTo(BigDecimal.valueOf(3)) < 0) {
            points += 100;
        } else if (interestCoverage.compareTo(BigDecimal.valueOf(5)) < 0) {
            points += 140;
        } else {
            points += 166.67;
        }

        // Net Debt Ratio
        if (netDebtRatio.compareTo(BigDecimal.valueOf(5)) > 0) {
        } else if (netDebtRatio.compareTo(BigDecimal.valueOf(4)) >0) {
            points += 50;
        } else if (netDebtRatio.compareTo(BigDecimal.valueOf(3)) > 0) {
            points += 100;
        } else if (netDebtRatio.compareTo(BigDecimal.valueOf(2)) > 0) {
            points += 140;
        } else {
            points += 166.67;
        }

        // Net Profitability
        if (netProfitability.compareTo(BigDecimal.valueOf(0)) < 0) {
        } else if (netProfitability.compareTo(BigDecimal.valueOf(5)) <0) {
            points += 50;
        } else if (netProfitability.compareTo(BigDecimal.valueOf(10)) < 0) {
            points += 100;
        } else if (netProfitability.compareTo(BigDecimal.valueOf(15)) < 0) {
            points += 140;
        } else {
            points += 166.67;
        }

        // Change in Sales Revenue
        if (changeInSalesRevenue.compareTo(BigDecimal.valueOf(-10)) < 0) {
        } else if (changeInSalesRevenue.compareTo(BigDecimal.valueOf(0)) < 0) {
            points += 50;
        } else if (changeInSalesRevenue.compareTo(BigDecimal.valueOf(5)) < 0) {
            points += 100;
        } else if (changeInSalesRevenue.compareTo(BigDecimal.valueOf(15)) < 0) {
            points += 140;
        } else {
            points += 166.67;
        }

        if (points > 1000) {
            points = 1000;
        }
        // BigDecimal totalScore =BigDecimal.valueOf(Math.min(10, (int) Math.floor((1000 - points) / 100) + 1));
        BigDecimal totalScore = BigDecimal.valueOf(points);
        map.put("totalScore", totalScore);
        map.put("quickLiquidityRatio", quickLiquidityRatio);
        map.put("equityRatio", equityRatio);
        map.put("interestCoverage", interestCoverage);
        map.put("netDebtRatio", netDebtRatio);
        map.put("netProfitability", netProfitability);
        map.put("changeInSalesRevenue", changeInSalesRevenue);

        return map;
    }

    // --------------------------------------------------------------------------------------------------------------

    public Integer getLastPageInfo(){
        List<Company> companies = companyRepository.findAll();
        int lastPage = (int)Math.ceil(companies.size()/NUM_ELEMENTS_PER_PAGE);
        return lastPage;
    }
    public Company getCompany(Long id) throws EntityNotFoundException{
        Company company = companyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Company not found"));
        return company;
    }
    public Company getCompanybyId(String id) throws EntityNotFoundException{
        Company company = companyRepository.findByCompanyId(id).orElseThrow(() -> new EntityNotFoundException("Company not found"));
        return company;
    }
    // pagal imones table id
    public UpdateCompanyDataDTO getCompanyData(Long id) throws EntityNotFoundException{
        CompanyData companyData = companyDataRepo.findByCompany_Id(id).orElseThrow(() -> new EntityNotFoundException("Company not found"));
        UpdateCompanyDataDTO sendData = UpdateCompanyDataDTO.from(companyData);
        return sendData;
    }

    public List<Company> getCompanyList() {
        List<Company> companies = companyRepository.findAll();
        return companies;
    }
    public List<Company> getListPaged(Long page){
        int numPerPage = NUM_ELEMENTS_PER_PAGE;
        List<Company> companies =companyRepository.findAll();
        List<Company> pagedList = new ArrayList<Company>();
        int pageStart = numPerPage*page.intValue();
        int pageEnd = Math.min(companies.size(),pageStart+numPerPage);
        for(int i = pageStart;i<pageEnd;i++){
            pagedList.add(companies.get(i));
        } 
        return pagedList;
    }

    public List<Document> getDocumentList(Long companyId) throws EntityNotFoundException{
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        return company.getDocuments();
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

    // --------------------------------------------------------------------------------------------------------------

    public Long createCompany(CompanyDTO dto) {
        Company company = Company.from(dto);
        Company saved = companyRepository.save(company);
        CompanyData newData = new CompanyData(saved);
        companyDataRepo.save(newData);
        return saved.getId();
    }

    public Long saveDocument(Long companyId, MultipartFile file) throws IOException, EntityNotFoundException{
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new EntityNotFoundException("Person not found."));

        String filename = storeDocument(file);
        String originalName = file.getOriginalFilename();
        Document document = new Document();
        document.setFilename(filename);
        document.setContentType(file.getContentType());
        document.setIndividual(company);
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
    public boolean readDataFromPDF(Long companyId) throws IOException{
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        List<Document> docs = company.getDocuments();
        if (docs.isEmpty()) {
            throw new RuntimeException("No documents found");
        }
        Document doc = docs.getLast(); 
        if(doc.getFilename().endsWith(".xhtml")) {
            org.jsoup.nodes.Document parsedHtml =
            Jsoup.parse(retrieveDocument(doc).getInputStream(), "UTF-8", "");
            System.out.println("---- .xhtml TEXT START ----");
            Elements rows = parsedHtml.select("tr");
            int pinigaiirPiniguEkvivalentai = 0;
            int atsargos = 0;
            int trumpalaikiaiIsipareigojimai = 0;

            int nuosavasKapitalas =0;
            int visasTurtas = 0;

            int grynasisPelnas =0;
            int palukanos = 0;
            int mokesciai =0;
            //int palukanuSanaudos = 0;

            int finansiniaiIsipareigojimai = 0;
            int pinigai = 0;
            int nusidevejimas = 0;
            int nusidevejimasSkips =0;
            
            int amortizacijaSkips =0;
            int amortizacija = 0;

            int pardavimai = 0;
            int pardavimaiOld = 0;

            for(int i  =0;i<rows.size();i++){
                String rowText = rows.get(i).text().toLowerCase();
                
                if(pinigaiirPiniguEkvivalentai==0)
                if(rowText.contains("pinigai ir pinigų ekvivalentai")){
                    String[] values = rows.get(i+1).text().split(" ");
                    pinigaiirPiniguEkvivalentai = Integer.parseInt((values[0]+values[1]));
                    System.out.println(pinigaiirPiniguEkvivalentai);
                }
                if(atsargos==0){
                 if(rowText.contains("atsargos")){
                    String[] values = rows.get(i).text().split(" ");
                    atsargos = Integer.parseInt((values[2]+values[3]));
                    System.out.println(atsargos);
                }   
                }
                 if(trumpalaikiaiIsipareigojimai==0){
                 if(rowText.contains("trumpalaikiai atidėjiniai")){
                    System.out.println(rows.get(i).text());
                    System.out.println(rows.get(i+1).text());
                    String[] values = rows.get(i+1).text().split(" ");
                    trumpalaikiaiIsipareigojimai = Integer.parseInt((values[0]+values[1]));
                    System.out.println(trumpalaikiaiIsipareigojimai);
                }   
                }

                if(nuosavasKapitalas==0){
                 if(rowText.contains("akcininkų nuosavybės iš")){
                    System.out.println(rows.get(i).text());
                    System.out.println(rows.get(i+1).text());
                    String[] values = rows.get(i).text().split(" ");
                    nuosavasKapitalas = Integer.parseInt((values[4]+values[5]));
                    System.out.println(nuosavasKapitalas);
                }   
                }
                if(visasTurtas==0){
                 if(rowText.contains("turto iš viso")){
                    String[] values = rows.get(i).text().split(" ");
                    visasTurtas = Integer.parseInt((values[3]+values[4]));
                    System.out.println(visasTurtas);
                }   
                }

                if(grynasisPelnas==0){
                 if(rowText.contains("grynasis pelnas/(nuostoliai)")){
                    String[] values = rows.get(i).text().split(" ");
                    grynasisPelnas = Integer.parseInt((values[2]+values[3]));
                    System.out.println(grynasisPelnas);
                }   
                }
                if(palukanos==0){
                 if(rowText.contains("finansinės veiklos sąnaudos")){
                    String[] values = rows.get(i).text().split(" ");
                    System.out.println(rows.get(i).text());
                    System.out.println(rows.get(i+1).text());
                    palukanos = Integer.parseInt((values[4].replace("(", "").strip()+values[5].substring(0,values[5].length()-1)));
                    System.out.println(palukanos);
                }   
                }
                if(mokesciai==0){
                 if(rowText.contains("pelno mokestis")){
                    String[] values = rows.get(i).text().split(" ");
                    System.out.println(rows.get(i).text());
                    System.out.println(rows.get(i+1).text());
                    mokesciai = Integer.parseInt((values[3].replace("(", "").strip()+values[4].substring(0,values[4].length()-1)));
                    System.out.println(mokesciai);
                }   
                }
                if(finansiniaiIsipareigojimai==0){
                 if(rowText.contains("finansinės skolos")){
                    String[] values = rows.get(i).text().split(" ");
                    System.out.println(rows.get(i).text());
                    System.out.println(rows.get(i+1).text());
                    finansiniaiIsipareigojimai = Integer.parseInt((values[3]+values[4]));
                    System.out.println(finansiniaiIsipareigojimai);
                }   
                }
                if(pinigai==0){
                 if(rowText.contains("pinigai ir pinigų ekvivalentai")){
                    String[] values = rows.get(i).text().split(" ");
                    System.out.println(rows.get(i).text());
                    System.out.println(rows.get(i+1).text());
                    pinigai = Integer.parseInt((values[5]+values[6]));
                    System.out.println(pinigai);
                }   
                }
                if(nusidevejimas==0){
                 if(rowText.contains("nusidėvėjimas")){
                    if(nusidevejimasSkips<11)nusidevejimasSkips+=1;
                    else {
                        String[] values = rows.get(i).text().split(" ");
                        System.out.println(rows.get(i).text());
                        System.out.println(rows.get(i+1).text());
                        nusidevejimas = Integer.parseInt((values[8].replace("(", "").strip()+values[9].substring(0,values[9].length()-1)));
                        System.out.println(nusidevejimas);
                    }
                }   
                }
                if(amortizacija==0){
                 if(rowText.contains("amortizacijos sąnaudos")){
                    if(amortizacijaSkips<3)amortizacijaSkips+=1;
                    else {
                        String[] values = rows.get(i).text().split(" ");
                        System.out.println(rows.get(i).text());
                        System.out.println(rows.get(i+1).text());
                        amortizacija = Integer.parseInt((values[2].replace("(", "").replace(")", "")).strip());
                        System.out.println(amortizacija);
                    }
                }   
                }

                if(pardavimai==0){
                 if(rowText.contains("pardavimai")){
                    String[] values = rows.get(i).text().split(" ");
                    System.out.println(rows.get(i).text());
                    System.out.println(rows.get(i+1).text());
                    pardavimai = Integer.parseInt((values[2]+values[3]));
                    pardavimaiOld = Integer.parseInt((values[4]+values[5]));
                    System.out.println(pardavimai);
                    System.out.println(pardavimaiOld);
                }   
                }
            }
            UpdateCompanyDataDTO updateCompany = new UpdateCompanyDataDTO(companyId, BigDecimal.valueOf(pinigaiirPiniguEkvivalentai),
                    BigDecimal.valueOf(atsargos),BigDecimal.valueOf(trumpalaikiaiIsipareigojimai),BigDecimal.valueOf(pinigai),
                    BigDecimal.valueOf(nuosavasKapitalas),BigDecimal.valueOf(visasTurtas),BigDecimal.valueOf(grynasisPelnas),
                    BigDecimal.valueOf(palukanos),BigDecimal.valueOf(mokesciai),BigDecimal.valueOf(finansiniaiIsipareigojimai),
                    BigDecimal.valueOf(nusidevejimas),BigDecimal.valueOf(amortizacija),BigDecimal.valueOf(pardavimai),BigDecimal.valueOf(pardavimaiOld));
            
            updateCompanyData(updateCompany);
            //System.out.println(parsedHtml);
            System.out.println("---- .xhtml TEXT END ----");
        }
        else {
            try{ PDDocument pdf = PDDocument.load(retrieveDocument(doc).getInputStream());
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdf);
            String[] lines = text.split("\\r?\\n");
            for(int i  =0;i<lines.length;i++){
              
            }
            }
            catch (IOException e){ System.out.println("failed with loading");}
        }

        return false;
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
    public Long updateCompanyData(UpdateCompanyDataDTO dto) throws EntityNotFoundException{
        CompanyData companyData = companyDataRepo.findByCompany_Id(dto.getCompanyId()).orElseThrow(() -> new EntityNotFoundException("Company not found"));
         companyData.setShortTermAssets(dto.getShortTermAssets());
         companyData.setInventory(dto.getInventory());
         companyData.setShortTermLiabilities(dto.getShortTermLiabilities());

         companyData.setEquity(dto.getEquity());
         companyData.setTotalAssets(dto.getTotalAssets());
                
         companyData.setNetProfit(dto.getNetProfit());
         companyData.setInterest(dto.getInterest());
         companyData.setTaxes(dto.getTaxes());
                
         companyData.setFinancialLiabilities(dto.getFinancialLiabilities());
         companyData.setCash(dto.getCash());
         companyData.setDepreciation(dto.getDepreciation());
         companyData.setAmortization(dto.getAmortization());

         companyData.setSalesRevenueCurrent(dto.getSalesRevenueCurrent());
         companyData.setSalesRevenue1YearOld(dto.getSalesRevenue1YearOld());
        CompanyData saved = companyDataRepo.save(companyData);
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