
package com.example.mainservice.controller;

import com.example.mainservice.entity.AppointmentType;
import com.example.mainservice.repository.AppointmentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/appointment-types")
@RequiredArgsConstructor
public class AppointmentTypeController {

    private final AppointmentTypeRepository typeRepository;

    // GET all appointment types
    @GetMapping
    public List<AppointmentType> getAllTypes() {
        return typeRepository.findAll();
    }

    @PostMapping
    public AppointmentType addType(@RequestBody AppointmentType type) {
        return typeRepository.save(type);
    }

}
