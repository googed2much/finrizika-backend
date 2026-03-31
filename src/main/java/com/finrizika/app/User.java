package com.finrizika.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

// Enumerator class for roles.
enum Role {
    INVESTOR,
    ADMINISTRATOR
}

/*
 * Entity for table "user".
 * paaiskinimai veliau...
 */
@Data
@Entity
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

    public User() { }

}
