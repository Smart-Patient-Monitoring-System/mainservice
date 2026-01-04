package com.example.mainservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    private LocalDate dateOfBirth;
    private String address;
    private String nicNo;
    private String gender;
    private String contactNo;
    private String guardianName;
    private String guardianContactNo;
    private String bloodType;

    @NotBlank(message = "Role is required")
    private String role; // PATIENT, DOCTOR, NURSE, ADMIN
}
