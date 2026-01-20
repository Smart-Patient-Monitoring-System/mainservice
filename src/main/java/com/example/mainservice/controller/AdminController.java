package com.example.mainservice.controller;

import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.service.DoctorService;
import com.example.mainservice.service.EmailService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/doctor/create")
    public ResponseEntity<?> createDoctorByAdmin(@Valid @RequestBody DoctorDTO doctorDto) {
        try {
            // Create the doctor
            Doctor doctor = doctorService.create(doctorDto);

            // Send credentials email to doctor
            try {
                emailService.sendDoctorCredentialsEmail(
                    doctor.getEmail(),
                    doctor.getName(),
                    doctor.getUsername(),
                    doctorDto.getPassword() // Use the plain password before encryption
                );
            } catch (Exception e) {
                // Log error but don't fail the request if email fails
                // The doctor is already created
                System.err.println("Failed to send credentials email: " + e.getMessage());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Doctor created successfully and credentials sent via email");
            response.put("doctor", doctor);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during doctor creation: " + e.getMessage()));
        }
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
    }
}
