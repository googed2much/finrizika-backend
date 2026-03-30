package com.finrizika.app;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

enum FileType{
    PDF,
    DOC,
    DOCX,
    IMAGE
}

@Data
@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate timestamp;

    @Enumerated(EnumType.STRING)
    private FileType type;
    private String path;
    private boolean parsed;

    @ManyToOne
    @JoinColumn(name = "individual_id", nullable = true)
    private Individual individual;

    public Document(){}

}
