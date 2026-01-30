// AdminPatientAssignController.java
package com.example.mainservice.controller;

import com.example.mainservice.entity.Patient;
import com.example.mainservice.service.PatientAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/patients")
@RequiredArgsConstructor
public class AdminPatientAssignController {

    private final PatientAssignmentService assignService;

    // Assign a specific patient via RR
    @PostMapping("/{patientId}/assign-round-robin")
    public Patient assignRoundRobin(@PathVariable Long patientId, @RequestParam String hospital) {
        return assignService.assignPatientRoundRobin(patientId, hospital);
    }

    // Assign next unassigned patient via RR
    @PostMapping("/assign-next")
    public Patient assignNext(@RequestParam String hospital) {
        return assignService.assignNextUnassigned(hospital);
    }
}
