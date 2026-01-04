package com.example.mainservice.service;
import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.repository.DoctorRepo;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepo doctorrepo;

    public Doctor create(DoctorDTO doctor){

        Doctor d=Doctor.builder()
                .name(doctor.getName())
                .email(doctor.getEmail())
                .nicNo(doctor.getNicNo())
                .doctorRegNo(doctor.getDoctorRegNo())
                .address(doctor.getAddress())
                .gender(doctor.getGender())
                .contactNo(doctor.getContactNo())
                .hospital(doctor.getHospital())
                .password(doctor.getPassword())
                .position(doctor.getPosition())
                .username(doctor.getUsername())
                .dateOfBirth(doctor.getDateOfBirth())
                .build();
        return  doctorrepo.save(d);

    }

    public List<Doctor> getDetails(){

        return doctorrepo.findAll().stream().map(d -> Doctor.builder()
                .name(d.getName())
                .email(d.getEmail())
                .nicNo(d.getNicNo())
                .doctorRegNo(d.getDoctorRegNo())
                .address(d.getAddress())
                .gender(d.getGender())
                .contactNo(d.getContactNo())
                .hospital(d.getHospital())
                .password(d.getPassword())
                .position(d.getPosition())
                .username(d.getUsername())
                .dateOfBirth(d.getDateOfBirth()).build()).toList();
    }
}
