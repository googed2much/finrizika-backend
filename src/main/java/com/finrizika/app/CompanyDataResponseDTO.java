package com.finrizika.app;

import lombok.Data;

@Data
public class CompanyDataResponseDTO {
    private Long id;
    private String companyCode;
    private String name;
    private String phoneNumber;
    private double score;
}