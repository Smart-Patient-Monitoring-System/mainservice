package com.example.mainservice.controller;

import com.example.mainservice.dto.CriticalAlertDTO;
import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.dto.DoctorPortalPatientDTO;
import com.example.mainservice.dto.ECGReadingDTO;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.entity.ECGReading;
import com.example.mainservice.entity.EmergencyAlert;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.repository.ECGReadingRepository;
import com.example.mainservice.repository.EmergencyAlertRepository;
import com.example.mainservice.repository.PatientRepo;
import com.example.mainservice.service.DoctorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller // initialize as a controller
@RestController // initialize rest API s
@RequestMapping("/api/doctor")
@CrossOrigin(origins = "http://localhost:5173")
public class SpecialDoctorsController {
}
