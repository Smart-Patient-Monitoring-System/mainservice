package com.example.mainservice.controller;


import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller //initialize as a controller
@RestController //initialize rest API s
@RequestMapping("/api/doctor")
public class DoctorController {
    @Autowired
    private DoctorService doctorservice;

    @PostMapping("/create")
    public Doctor createDoctor(@RequestBody DoctorDTO doctorDto){
        return doctorservice.create(doctorDto);
    }

    @GetMapping("/get")
    public List<DoctorDTO> getAllDocters(){
        return  doctorservice.getDetails();
    }

    @DeleteMapping("/delete/{Id}")
    public String deleteDoctorByRegistrationNo(@PathVariable Long Id) {
        try {
            doctorservice.deleteDoctor(Id);
            return "deleted successfully!";
        } catch (RuntimeException e) {
            return "Delete Failed";
        }
    }

}
