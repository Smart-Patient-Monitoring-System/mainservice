package com.example.mainservice.controller;


import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.service.DoctorService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller //initialize as a controller
@RestController //initialize rest API s
@RequestMapping("/api/doctor")
public class DoctorController {

    private DoctorService doctorservice;
    @PostMapping("/create")
    public Doctor createDoctor(@RequestBody DoctorDTO doctorDto){
        return doctorservice.create(doctorDto);
    }

}
