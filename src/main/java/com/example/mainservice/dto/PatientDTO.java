package com.example.mainservice.dto;

import lombok.*;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PatientDTO {
    private Long Id;

    private String name;

    private LocalDate dateOfBirth;

    private String address;

    private String email;

    private String nicNo;

    private String gender;

    private String contactNo;

    private String guardiansName;

    private String guardiansContactNo;

    private String username;

    private String password;

    private String bloodType;
}
