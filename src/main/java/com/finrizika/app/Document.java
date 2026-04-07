package com.finrizika.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private String contentType;
    private String originalName;
    @Column(nullable = false, columnDefinition="boolean default false")
    private boolean parsed = false;

    @ManyToOne
    @JoinColumn(name = "individual_id", nullable = true)
    private Individual individual;

    public Document(){}

}
