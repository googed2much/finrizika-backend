package com.finrizika.app;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

// Enumerator class for roles.
enum Role {
    INVESTOR,
    ADMINISTRATOR
}

/*
 * Entity for table "user".
 * paaiskinimai veliau...
 */
@Getter
@Setter
@Entity
@SQLRestriction("deleted = false")
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(length = 30, nullable = false, unique = false)
    private String email;
    @Column(length = 100, nullable = false, unique = false)
    private String password;
    @Column(length = 12, nullable = false, unique = false)
    private String telephone;
    @Column(length = 50, nullable = false, unique = false)
    private String fullname;
    @Column(length = 11, nullable = false, unique = true)
    private String citizenId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = false)
    private Role role;

    @Column(nullable = false, columnDefinition="boolean default false")
    private boolean deleted = false;

    public void softDelete() {
        this.deleted = true;
    }

    public User() { }

}
