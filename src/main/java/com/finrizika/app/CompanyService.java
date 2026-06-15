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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.finrizika.app.CompanyController.CompanyDTO;
import com.finrizika.app.CompanyController.UpdateCompanyDataDTO;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Service
public class CompanyService {

    private Long NUM_ELEMENTS_PER_PAGE = 10l;
    private final CompanyRepository companyRepository;
    private final DocumentRepository documentRepository;
    private final CompanyDataRepository companyDataRepo;
    @Value("${app.document.upload-directory}")
    private String storagePath;
    private Path documentStorageLocation;
    @Value("${fastapi.url}")
    private String fastapiUrl;

     public CompanyService(
        CompanyRepository companyRepository,
        DocumentRepository documentRepository,
        CompanyDataRepository companyDataRepo,
        RestTemplate restTemplate
    ) {
        this.companyRepository = companyRepository;
        this.documentRepository = documentRepository;
        this.companyDataRepo = companyDataRepo;
        this.restTemplate = restTemplate;
    }
    private final RestTemplate restTemplate;
    @PostConstruct
    public void initializeStorage() throws IOException{
        this.documentStorageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(this.documentStorageLocation);
    }

    // --------------------------------------------------------------------------------------------------------------

    public BigDecimal calculateQuickLiquidityRatio(BigDecimal shortTermAssets, BigDecimal inventory, BigDecimal shortTermLiabilities) {
        if (shortTermLiabilities == null || shortTermLiabilities.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; 
        }
        return (shortTermAssets.subtract(inventory)).divide(shortTermLiabilities, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateEquityRatio(BigDecimal equity, BigDecimal totalAssets) {
        if (totalAssets == null || totalAssets.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; 
        }
        return equity.divide(totalAssets,RoundingMode.HALF_UP);
    }

    public BigDecimal calculateEBIT(BigDecimal netProfit, BigDecimal interest, BigDecimal taxes) {
        return netProfit.add(taxes).add(interest);
    }

    public BigDecimal calculateInterestCoverage(BigDecimal ebit, BigDecimal interestExpenses) {
         if (interestExpenses == null || interestExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; 
        }
        return ebit.divide(interestExpenses,RoundingMode.HALF_UP);
    }

    public BigDecimal calculateNetDebtRatio(BigDecimal financialLiabilities, BigDecimal cash, BigDecimal ebit, BigDecimal depreciation,
            BigDecimal amortization) {
        if (ebit.add(depreciation).add(amortization) == null || ebit.add(depreciation).add(amortization).compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; 
        }
        return (financialLiabilities.subtract(cash)).divide((ebit.add(depreciation).add(amortization)),RoundingMode.HALF_UP);
    }

    public BigDecimal calculateNetProfitability(BigDecimal netProfit, BigDecimal salesRevenue) {
         if (salesRevenue == null || salesRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; 
        }
        return netProfit.divide(salesRevenue,RoundingMode.HALF_UP);
    }

    public BigDecimal calculateChangeInSalesRevenue(BigDecimal yearOldRev, BigDecimal currentRev) {
        if (yearOldRev == null || yearOldRev.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; 
        }
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

        double quickLiquidityPoints = 0;
        double equityRatioPoints = 0;
        double interestCoveragePoints = 0;
        double netDebtRatioPoints = 0;
        double netProfitabilityPoints = 0;
        double salesRevenuePoints = 0;

        // Quick Liquidity Ratio
        if (quickLiquidityRatio.compareTo(BigDecimal.valueOf(0.8)) < 0) {
        } else if (quickLiquidityRatio.compareTo(BigDecimal.valueOf(1)) < 0) {
            quickLiquidityPoints = 50;
        } else if (quickLiquidityRatio.compareTo(BigDecimal.valueOf(1.2)) < 0) {
            quickLiquidityPoints = 100;
        } else if (quickLiquidityRatio.compareTo(BigDecimal.valueOf(1.5)) < 0) {
            quickLiquidityPoints = 140;
        } else {
            quickLiquidityPoints = 166.67;
        }

        points += quickLiquidityPoints;

        // Equity Ratio
        if (equityRatio.compareTo(BigDecimal.valueOf(0.2)) < 0) {
        } else if (equityRatio.compareTo(BigDecimal.valueOf(0.3)) < 0) {
            equityRatioPoints = 50;
        } else if (equityRatio.compareTo(BigDecimal.valueOf(0.4)) < 0) {
            equityRatioPoints = 100;
        } else if (equityRatio.compareTo(BigDecimal.valueOf(0.5)) < 0) {
            equityRatioPoints = 140;
        } else {
            equityRatioPoints = 166.67;
        }
        points += equityRatioPoints;
        // Interest Coverage
        if (interestCoverage.compareTo(BigDecimal.valueOf(1)) < 0) {
        } else if (interestCoverage.compareTo(BigDecimal.valueOf(2)) <0) {
            interestCoveragePoints = 50;
        } else if (interestCoverage.compareTo(BigDecimal.valueOf(3)) < 0) {
            interestCoveragePoints = 100;
        } else if (interestCoverage.compareTo(BigDecimal.valueOf(5)) < 0) {
            interestCoveragePoints = 140;
        } else {
            interestCoveragePoints = 166.67;
        }
        points += interestCoveragePoints;
        // Net Debt Ratio
        if (netDebtRatio.compareTo(BigDecimal.valueOf(5)) > 0) {
        } else if (netDebtRatio.compareTo(BigDecimal.valueOf(4)) >0) {
            netDebtRatioPoints = 50;
        } else if (netDebtRatio.compareTo(BigDecimal.valueOf(3)) > 0) {
            netDebtRatioPoints = 100;
        } else if (netDebtRatio.compareTo(BigDecimal.valueOf(2)) > 0) {
            netDebtRatioPoints = 140;
        } else {
            netDebtRatioPoints = 166.67;
        }
        points += netDebtRatioPoints;
        // Net Profitability
        if (netProfitability.compareTo(BigDecimal.valueOf(0)) < 0) {
        } else if (netProfitability.compareTo(BigDecimal.valueOf(5)) <0) {
            netProfitabilityPoints = 50;
        } else if (netProfitability.compareTo(BigDecimal.valueOf(10)) < 0) {
            netProfitabilityPoints = 100;
        } else if (netProfitability.compareTo(BigDecimal.valueOf(15)) < 0) {
            netProfitabilityPoints = 140;
        } else {
            netProfitabilityPoints = 166.67;
        }
        points += netProfitabilityPoints;
        // Change in Sales Revenue
        if (changeInSalesRevenue.compareTo(BigDecimal.valueOf(-10)) < 0) {
        } else if (changeInSalesRevenue.compareTo(BigDecimal.valueOf(0)) < 0) {
            salesRevenuePoints = 50;
        } else if (changeInSalesRevenue.compareTo(BigDecimal.valueOf(5)) < 0) {
            salesRevenuePoints = 100;
        } else if (changeInSalesRevenue.compareTo(BigDecimal.valueOf(15)) < 0) {
            salesRevenuePoints = 140;
        } else {
            salesRevenuePoints = 166.67;
        }
        points+= salesRevenuePoints;
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
        map.put("quickLiquidityPoints", BigDecimal.valueOf(quickLiquidityPoints));
        map.put("equityRatioPoints", BigDecimal.valueOf(equityRatioPoints));
        map.put("interestCoveragePoints", BigDecimal.valueOf(interestCoveragePoints));
        map.put("netDebtRatioPoints", BigDecimal.valueOf(netDebtRatioPoints));
        map.put("netProfitabilityPoints", BigDecimal.valueOf(netProfitabilityPoints));
        map.put("salesRevenuePoints", BigDecimal.valueOf(salesRevenuePoints));

        return map;
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number n) return n.doubleValue();
        double val = Double.parseDouble(value.toString());
        return Math.abs(val);
    }
    
    public boolean readDataFromFile(Long companyId) throws IOException, RuntimeException{
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        List<Document> docs = company.getDocuments();
        if (docs.size() < 1) {
            throw new RuntimeException("Reikalingi bent 1 dokumentas: įmonės apskaita ar kredito istorija");
        }
        Document primarySource = docs.getLast();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(Paths.get("uploads", "documents", primarySource.getFilename())));

        String companyJobId = "";
        boolean fastApi = true;
        try {
            companyJobId = restTemplate.postForObject(
                fastapiUrl + "/api/read/company",
                body,
                Map.class
            ).get("job_id").toString();
        }
        catch (Exception e) {
            System.out.println("FastAPI unavailable");
            fastApi=false;
        }
        if (!fastApi) {
            readDataFromSpecificFile(companyId);
            return false;
        }
        Map companyResult;
        Map creditResult;
        System.out.println("Job ID: " + companyJobId);
        while (true) {
            companyResult = restTemplate.getForObject(
                fastapiUrl+"/api/result/" + companyJobId,
                Map.class
            );

            Object status = companyResult.get("status");
            if (status == null || !"pending".equals(status.toString())) break;
           try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); 
                throw new RuntimeException("Polling interrupted", e);
            }
            if(!fastApi) break;
          
        }

        /*
        MultiValueMap<String, Object> body2 = new LinkedMultiValueMap<>();
        body2.add("file", new FileSystemResource(Paths.get("uploads", "documents", creditInfoSource.getFilename())));
        try {
        companyJobId = restTemplate.postForObject(
            fastapiUrl + "/api/read/company",
            body2,
            Map.class
        ).get("job_id").toString();
        System.out.println("Job ID: " + companyJobId);
        }
        catch(Exception e){
            System.out.println("fastapi not working");
            fastApi= false;
        }
        while (true) {
            creditResult = restTemplate.getForObject(
                fastapiUrl + "/api/read/company" + "/api/result/" + companyJobId,
                Map.class
            );

            Object creditStatus = creditResult.get("status");
            if (creditStatus == null || !"pending".equals(creditStatus.toString())) break;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); 
                throw new RuntimeException("Polling interrupted", e);
            }
            if(!fastApi) break;
          
        }
        */
        System.out.println("---------Imones apskaitos result -----");
        System.out.println("Company result: " + companyResult);

        double trumpalaikisTurtas = toDouble(companyResult.get("trumpalaikis turtas"));
        double atsargos = toDouble(companyResult.get("atsargos"));

        double trumpalaikiaiIsipareigojimai = toDouble(companyResult.get("trumpalaikiai įsipareigojimai"));

        double nuosavasKapitalas = toDouble(companyResult.get("nuosavas kapitalas"));

        double visasTurtas = toDouble(companyResult.get("visas turtas"));

        double grynasisPelnas = toDouble(companyResult.get("grynas pelnas"));

        double palukanos = toDouble(companyResult.get("palūkanos"));

        double mokesciai = toDouble(companyResult.get("sumokėti mokesčiai"));

        double finansiniaiIsipareigojimai =toDouble(companyResult.get("finansiniai įsipareigojimai"));

        double pinigai =toDouble(companyResult.get("grynieji pinigai"));

        double nusidevejimas = toDouble(companyResult.get("nusidėvejimas"));

        double amortizacija = toDouble(companyResult.get("amortizacija"));

        double pardavimai = toDouble(companyResult.get("pardavimų pajamos"));

        double pardavimaiOld = toDouble(companyResult.get("pardavimų pajamos praeitų metų"));
        
        UpdateCompanyDataDTO updateCompany = new UpdateCompanyDataDTO(companyId, BigDecimal.valueOf(trumpalaikisTurtas),
            BigDecimal.valueOf(atsargos),BigDecimal.valueOf(trumpalaikiaiIsipareigojimai),BigDecimal.valueOf(pinigai),
            BigDecimal.valueOf(nuosavasKapitalas),BigDecimal.valueOf(visasTurtas),BigDecimal.valueOf(grynasisPelnas),
            BigDecimal.valueOf(palukanos),BigDecimal.valueOf(mokesciai),BigDecimal.valueOf(finansiniaiIsipareigojimai),
            BigDecimal.valueOf(nusidevejimas),BigDecimal.valueOf(amortizacija),BigDecimal.valueOf(pardavimai),BigDecimal.valueOf(pardavimaiOld));
    
        updateCompanyData(updateCompany);
        if(primarySource.getFilename().contains("rokiskio")) readDataFromSpecificFile(companyId);
        double creditResultPardavimai = toDouble(creditResult.get("pardavimų pajamos"));
        return creditResultPardavimai==pardavimai;
    }
    public boolean readDataFromSpecificFile(Long companyId) throws IOException, RuntimeException{
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        List<Document> docs = company.getDocuments();
        if (docs.isEmpty()) {
            throw new RuntimeException("No documents found");
        }
        Document doc = docs.getLast();
        if(doc.getFilename().endsWith(".xhtml")) {
            org.jsoup.nodes.Document parsedHtml = Jsoup.parse(retrieveDocument(doc).getInputStream(), "UTF-8", "");
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
                    // String[] values = rows.get(i+1).text().split(" ");
                    Element row = rows.get(i+1);
                    Elements els = row.select("td,th");
                    
                    pinigaiirPiniguEkvivalentai = Integer.parseInt((els.get(2).text().replaceAll(" ", "")));
                    System.out.println(pinigaiirPiniguEkvivalentai);
                    for (Element el : els) {
                        System.out.println(el.toString());
                    };
                }
                if(atsargos==0){
                 if(rowText.contains("atsargos")){
                    // String[] values = rows.get(i).text().split(" ");
                    Element row = rows.get(i);
                    Elements els = row.select("td,th");
                    
                    atsargos = Integer.parseInt((els.get(2).text().replaceAll(" ", "")));
                    System.out.println(atsargos);
                }   
                }
                 if(trumpalaikiaiIsipareigojimai==0){
                 if(rowText.contains("trumpalaikiai atidėjiniai")){
                    System.out.println(rows.get(i).text());
                    System.out.println(rows.get(i+1).text());
                    // String[] values = rows.get(i+1).text().split(" ");
                    Element row = rows.get(i+1);
                    Elements els = row.select("td,th");
                    
                    trumpalaikiaiIsipareigojimai = Integer.parseInt(els.get(2).text().replaceAll(" ", ""));
                    System.out.println(trumpalaikiaiIsipareigojimai);
                }   
                }

                if(nuosavasKapitalas==0){
                 if(rowText.contains("akcininkų nuosavybės iš")){
                    System.out.println(rows.get(i).text());
                    System.out.println(rows.get(i+1).text());
                    // String[] values = rows.get(i).text().split(" ");
                    Element row = rows.get(i);
                    Elements els = row.select("td,th");
                    
                    nuosavasKapitalas = Integer.parseInt(els.get(2).text().replaceAll(" ", ""));
                    System.out.println(nuosavasKapitalas);
                }   
                }
                if(visasTurtas==0){
                 if(rowText.contains("turto iš viso")){
                    // String[] values = rows.get(i).text().split(" ");
                    Element row = rows.get(i);
                    Elements els = row.select("td,th");
                    
                    visasTurtas = Integer.parseInt(els.get(2).text().replaceAll(" ", ""));
                    System.out.println(visasTurtas);
                }   
                }

                if(grynasisPelnas==0){
                 if(rowText.contains("grynasis pelnas/(nuostoliai)")){
                    // String[] values = rows.get(i).text().split(" ");
                     Element row = rows.get(i);
                    Elements els = row.select("td,th");
                    
                    grynasisPelnas = Integer.parseInt(els.get(2).text().replaceAll(" ", ""));
                   System.out.println(grynasisPelnas);
                }   
                }
                if(palukanos==0){
                 if(rowText.contains("finansinės veiklos sąnaudos")){
                    // String[] values = rows.get(i).text().split(" ");
                    System.out.println(rows.get(i).text());
                    System.out.println(rows.get(i+1).text());
                    Element row = rows.get(i);
                    Elements els = row.select("td,th");
                  
                    // palukanos = Integer.parseInt((values[4].replace("(", "").strip()+values[5].substring(0,values[5].length()-1)));
                    palukanos = Integer.parseInt(els.get(2).text().replaceAll(" ", "").replaceAll("\\(", "").replaceAll("\\)", ""));
                    System.out.println(palukanos);
                }   
                }
                if(mokesciai==0){
                 if(rowText.contains("pelno mokestis")){
                    // String[] values = rows.get(i).text().split(" ");
                    System.out.println(rows.get(i).text());
                    System.out.println(rows.get(i+1).text());
                    Element row = rows.get(i);
                    Elements els = row.select("td,th");
                  
                    // palukanos = Integer.parseInt((values[4].replace("(", "").strip()+values[5].substring(0,values[5].length()-1)));
                    mokesciai = Integer.parseInt(els.get(2).text().replaceAll(" ", "").replaceAll("\\(", "").replaceAll("\\)", ""));
                    System.out.println(mokesciai);
                }   
                }
                if(finansiniaiIsipareigojimai==0){
                 if(rowText.contains("finansinės skolos")){
                    // String[] values = rows.get(i).text().split(" ");
                    System.out.println(rows.get(i).text());
                    System.out.println(rows.get(i+1).text());
                     Element row = rows.get(i);
                    Elements els = row.select("td,th");
                    
                    finansiniaiIsipareigojimai = Integer.parseInt(els.get(2).text().replaceAll(" ", ""));
                   System.out.println(finansiniaiIsipareigojimai);
                }   
                }
                if(pinigai==0){
                 if(rowText.contains("pinigai ir pinigų ekvivalentai")){
                    // String[] values = rows.get(i).text().split(" ");
                    System.out.println(rows.get(i).text());
                    System.out.println(rows.get(i+1).text());
                      Element row = rows.get(i);
                    Elements els = row.select("td,th");
                    
                    pinigai = Integer.parseInt(els.get(2).text().replaceAll(" ", ""));
                   System.out.println(pinigai);
                }   
                }
                if(nusidevejimas==0){
                 if(rowText.contains("nusidėvėjimas")){
                    if(nusidevejimasSkips<11)nusidevejimasSkips+=1;
                    else {
                        // String[] values = rows.get(i).text().split(" ");
                        System.out.println(rows.get(i).text());
                        System.out.println(rows.get(i+1).text());
                         Element row = rows.get(i);
                    Elements els = row.select("td,th");
                  
                    // palukanos = Integer.parseInt((values[4].replace("(", "").strip()+values[5].substring(0,values[5].length()-1)));
                    nusidevejimas = Integer.parseInt(els.last().text().replaceAll(" ", "").replaceAll("\\(", "").replaceAll("\\)", ""));
                   System.out.println(nusidevejimas);
                    }
                }   
                }
                if(amortizacija==0){
                 if(rowText.contains("amortizacijos sąnaudos")){
                    if(amortizacijaSkips<3)amortizacijaSkips+=1;
                    else {
                        // String[] values = rows.get(i).text().split(" ");
                        System.out.println(rows.get(i).text());
                        System.out.println(rows.get(i+1).text());
                             Element row = rows.get(i);
                    Elements els = row.select("td,th");
                  
                    // palukanos = Integer.parseInt((values[4].replace("(", "").strip()+values[5].substring(0,values[5].length()-1)));
                    amortizacija = Integer.parseInt(els.get(1).text().replaceAll(" ", "").replaceAll("\\(", "").replaceAll("\\)", ""));
                   System.out.println(amortizacija);
                    }
                }   
                }

                if(pardavimai==0){
                 if(rowText.contains("pardavimai")){
                    // String[] values = rows.get(i).text().split(" ");
                    System.out.println(rows.get(i).text());
                    System.out.println(rows.get(i+1).text());
                    Element row = rows.get(i);
                    Elements els = row.select("td,th");
                 
                    pardavimai = Integer.parseInt(els.get(2).text().replaceAll(" ", ""));
                    pardavimaiOld = Integer.parseInt(els.get(3).text().replaceAll(" ",""));
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

    @Transactional(readOnly = true)
    public Long getLastPageInfo(){
        List<Company> companies = companyRepository.findAll();
        Long lastPage =(companies.size()+NUM_ELEMENTS_PER_PAGE-1)/NUM_ELEMENTS_PER_PAGE;
        return lastPage;
    }

    @Transactional(readOnly = true)
    public Company getCompany(Long id) throws EntityNotFoundException{
        Company company = companyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Company not found"));
        return company;
    }

    @Transactional(readOnly = true)
    public Company getCompanybyId(String id) throws EntityNotFoundException{
        Company company = companyRepository.findByCompanyId(id).orElseThrow(() -> new EntityNotFoundException("Company not found"));
        return company;
    }

    @Transactional(readOnly = true)
    public UpdateCompanyDataDTO getCompanyData(Long id) throws EntityNotFoundException{
        CompanyData companyData = companyDataRepo.findByCompany_Id(id).orElseThrow(() -> new EntityNotFoundException("Company not found"));
        UpdateCompanyDataDTO sendData = UpdateCompanyDataDTO.from(companyData);
        return sendData;
    }

    @Transactional(readOnly = true)
    public List<Company> getCompanyList() {
        List<Company> companies = companyRepository.findAll();
        return companies;
    }

    @Transactional(readOnly = true)
    public List<Company> getListPaged(Long page){
        List<Company> companies = companyRepository.findAll();

        List<Company> pagedList = new ArrayList<Company>();

        Long pageStart = NUM_ELEMENTS_PER_PAGE * page;
        Long pageEnd = Math.min(companies.size(), pageStart + NUM_ELEMENTS_PER_PAGE);

        for(Long i = pageStart; i<pageEnd; i++){
            pagedList.add(companies.get(i.intValue()));
        }

        return pagedList;
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentList(Long companyId) throws EntityNotFoundException{
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new EntityNotFoundException("Person not found."));
        return company.getDocuments();
    }

    @Transactional(readOnly = true)
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

    @Transactional
    public Long createCompany(CompanyDTO dto) {
        Company company = Company.from(dto);
        Company saved = companyRepository.save(company);
        CompanyData companyData = CompanyData.empty();
        companyData.setCompany(company);
        companyDataRepo.save(companyData);
        return saved.getId();
    }

    @Transactional
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

    // --------------------------------------------------------------------------------------------------------------

    @Transactional
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

    @Transactional
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

    @Transactional
    public Long deleteCompany(Long id) throws EntityNotFoundException{
        Company company = companyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Company not found"));
        company.softDelete();
        Company saved = companyRepository.save(company);
        return saved.getId();
    }

}