package com.example.mainservice.repository;
import com.example.mainservice.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepo extends JpaRepository<Doctor,Long> {

}
