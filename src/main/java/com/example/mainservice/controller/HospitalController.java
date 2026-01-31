
package com.example.mainservice.controller;

import com.example.mainservice.entity.Hospital;
import com.example.mainservice.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalRepository hospitalRepo;

    @GetMapping("/hospitals")
    public List<String> getHospitals() {
        return hospitalRepo.findAll()
                .stream()
                .map(Hospital::getName)   // make sure Hospital has getName()
                .sorted()
                .collect(Collectors.toList());
    }
}
