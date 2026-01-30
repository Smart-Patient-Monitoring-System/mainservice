// DoctorPatientsController.java
package com.example.mainservice.controller;

import com.example.mainservice.entity.Doctor;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.PatientRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorPatientsController {

    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;

    @GetMapping("/patients")
    public List<Patient> getMyPatients(Authentication auth) {
        String email = auth.getName(); // JWT subject (sub)
        Doctor doctor = doctorRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return patientRepo.findByDoctorId(doctor.getId());
    }
}
