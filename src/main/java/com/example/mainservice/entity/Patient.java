package com.example.mainservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor

public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // DB primary key

    @Column(nullable = false)
    private String name;

    private LocalDate dateOfBirth;

    private String address;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nicNo;

    private String gender;

    @Column(nullable = false)
    private String contactNo;

    @Column(nullable = false, unique = true)
    private String guardiansName;

    @Column(nullable = false)
    private String guardiansContactNo;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String bloodType;

    public Patient() {

    }
}
