package com.example.mainservice.controller;

import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.dto.PatientDTO;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller //initialize as a controller
@RestController //initialize rest API s
@RequestMapping("/api/patient")
public class PatientController {
    @Autowired
    private PatientService patientservice;

    @PostMapping("/create")
    public Patient createPatient(@RequestBody PatientDTO patientDto){
        return patientservice.create(patientDto);
    }

    @GetMapping("/get")
    public List<PatientDTO> getAllPatients(){
        return  patientservice.getDetails();
    }

    @DeleteMapping("/delete/{Id}")
    public String deletePatientByID(@PathVariable Long Id) {
        try {
            patientservice.deletePatient(Id);
            return "deleted successfully!";
        } catch (RuntimeException e) {
            return "Delete Failed";
        }
    }
}
