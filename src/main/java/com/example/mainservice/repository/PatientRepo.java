package com.example.mainservice.repository;

import com.example.mainservice.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepo extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUsername(String username);

    Optional<Patient> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<Patient> findByDoctorId(Long doctorId);

    List<Patient> findByHospitalAndDoctorIsNull(String hospital);
    Optional<Patient> findTopByHospitalAndDoctorIsNullOrderByIdAsc(String hospital);
}
