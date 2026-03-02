package com.finrizika.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long ID;

    @Column(length = 30, nullable = false, unique = false)
    private String Email;

    @Column(length = 25, nullable = false, unique = false)
    private String Password;

    @Column(length = 20, nullable = false, unique = false)
    private String PasswordSalt;

    @Column(nullable = false, unique = false)
    private long Telephone;

}
