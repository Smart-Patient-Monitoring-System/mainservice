package com.example.mainservice.service;

import com.example.mainservice.dto.PatientDTO;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.repository.PatientRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepo patientrepo;

    public Patient create(PatientDTO patient){

        Patient p =Patient.builder()
                .id(patient.getId())
                .name(patient.getName())
                .dateOfBirth(patient.getDateOfBirth())
                .address(patient.getAddress())
                .email(patient.getEmail())
                .nicNo(patient.getNicNo())
                .gender(patient.getGender())
                .contactNo(patient.getContactNo())
                .guardiansName(patient.getGuardiansName())
                .guardiansContactNo(patient.getGuardiansContactNo())
                .username(patient.getUsername())
                .password(patient.getPassword())
                .bloodType(patient.getBloodType())
                .build();
        return patientrepo.save(p);

    }

    public List<PatientDTO> getDetails(){
        return patientrepo.findAll().stream().map( p -> PatientDTO.builder()
                .Id(p.getId())
                .name(p.getName())
                .dateOfBirth(p.getDateOfBirth())
                .address(p.getAddress())
                .email(p.getEmail())
                .nicNo(p.getNicNo())
                .gender(p.getGender())
                .contactNo(p.getContactNo())
                .guardiansName(p.getGuardiansName())
                .guardiansContactNo(p.getGuardiansContactNo())
                .bloodType(p.getBloodType())
                .password(p.getPassword())
                .username(p.getUsername())
                .build()).toList();
    }

    public void deletePatient(Long Id){

        patientrepo.deleteById(Id);
    }

}
