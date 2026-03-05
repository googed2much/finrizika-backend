package com.finrizika.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// Enumerator class for roles.
enum Role {
    INVESTOR,
    ADMINISTRATOR
}

/*
    Entity for table "user".
    paaiskinimai veliau...
*/
@Entity
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 30, nullable = false, unique = false)
    private String email;

    @Column(length = 100, nullable = false, unique = false)
    private String password;

    @Column(length = 12, nullable = false, unique = false)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = false)
    private Role role;

    public User() {}

    // ID
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // Email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Telephone
    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    // Role
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
