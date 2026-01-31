// PatientAssignmentService.java
package com.example.mainservice.service;

import com.example.mainservice.entity.*;
import com.example.mainservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientAssignmentService {

    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;
    private final HospitalAssignStateRepo stateRepo;


    @Transactional
    public Patient assignPatientRoundRobin(Long patientId, String hospital) {

        // 1) get patient
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // already assigned
        if (patient.getDoctor() != null) {
            return patient;
        }

        //  Prefer patient's stored hospital; fallback to parameter
        String hosp = (patient.getHospital() != null) ? patient.getHospital() : hospital;
        if (hosp == null || hosp.isBlank()) throw new RuntimeException("Hospital is required");

        // 2) doctors in that hospital
        List<Doctor> doctors = doctorRepo.findByHospitalOrderByIdAsc(hosp);
        if (doctors.isEmpty()) {
            throw new RuntimeException("No doctors in this hospital: " + hosp);
        }

        // 3) lock state row for hospital (prevents race condition)
        HospitalAssignState state = stateRepo.findByHospitalForUpdate(hosp)
                .orElseGet(() -> stateRepo.save(
                        HospitalAssignState.builder()
                                .hospital(hosp)
                                .lastDoctorId(null)
                                .build()
                ));

        Long lastDoctorId = state.getLastDoctorId();

        // 4) pick next doctor
        Doctor nextDoctor = pickNext(doctors, lastDoctorId);

        // 5) assign and update state
        patient.setDoctor(nextDoctor);
        patient.setHospital(hosp); //  ensure consistent hospital value

        state.setLastDoctorId(nextDoctor.getId());

        // save both
        stateRepo.save(state);
        return patientRepo.save(patient);
    }


    private Doctor pickNext(List<Doctor> doctors, Long lastDoctorId) {
        if (lastDoctorId == null) return doctors.get(0);

        for (int i = 0; i < doctors.size(); i++) {
            if (doctors.get(i).getId().equals(lastDoctorId)) {
                return doctors.get((i + 1) % doctors.size());
            }
        }
        // lastDoctorId not found -> start from first
        return doctors.get(0);
    }

    // Optional: assign "next unassigned patient" (admin button)
    @Transactional
    public Patient assignNextUnassigned(String hospital) {
        Patient p = patientRepo.findTopByHospitalAndDoctorIsNullOrderByIdAsc(hospital)
                .orElseThrow(() -> new RuntimeException("No unassigned patients"));
        return assignPatientRoundRobin(p.getId(), hospital);
    }
}
