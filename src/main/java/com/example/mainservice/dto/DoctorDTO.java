package com.example.mainservice.dto;
import lombok.*;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DoctorDTO {
    private String name;

    private LocalDate dateOfBirth;

    private String address;

    private String email;

    private String nicNo;

    private String gender;

    private String contactNo;

    private String doctorRegNo;

    private String position;

    private String hospital;

    private String username;

    private String password;
}
