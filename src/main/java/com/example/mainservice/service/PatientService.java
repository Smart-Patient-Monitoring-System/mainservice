package com.example.mainservice.service;

import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.dto.PatientDTO;
import com.example.mainservice.entity.Doctor;
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

    public PatientDTO updatePatient(Long Id, PatientDTO dto){
        Patient p = patientrepo.findById(Id).orElseThrow();

        if(dto.getName()!=null) p.setName(dto.getName());
        if(dto.getDateOfBirth()!=null) p.setDateOfBirth(dto.getDateOfBirth());
        if(dto.getAddress()!=null) p.setAddress(dto.getAddress());
        if(dto.getEmail()!=null) p.setEmail(dto.getEmail());
        if(dto.getNicNo()!=null) p.setNicNo(dto.getNicNo());
        if(dto.getGender()!=null) p.setGender(dto.getGender());
        if(dto.getContactNo()!=null) p.setContactNo(dto.getContactNo());
        if(dto.getGuardiansName()!=null) p.setGuardiansName(dto.getGuardiansName());
        if(dto.getGuardiansContactNo()!=null) p.setGuardiansContactNo(dto.getGuardiansContactNo());
        if(dto.getBloodType()!=null) p.setBloodType(dto.getBloodType());
        if(dto.getPassword()!=null) p.setPassword(dto.getPassword());
        if(dto.getUsername()!=null) p.setUsername(dto.getUsername());

        Patient updatedPatient = patientrepo.save(p);
        return convertToDTO(updatedPatient);


    }

    private PatientDTO convertToDTO(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        dto.setName(patient.getName());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setAddress(patient.getAddress());
        dto.setEmail(patient.getEmail());
        dto.setNicNo(patient.getNicNo());
        dto.setContactNo(patient.getContactNo());
        dto.setGender(patient.getGender());
        dto.setGuardiansName(patient.getGuardiansName());
        dto.setGuardiansContactNo(patient.getGuardiansContactNo());
        dto.setBloodType(patient.getBloodType());
        dto.setPassword(patient.getPassword());
        dto.setUsername(patient.getUsername());

        return dto;
    }
}
