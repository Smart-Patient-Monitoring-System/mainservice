// HospitalAssignState.java
package com.example.mainservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "hospital_assign_state", uniqueConstraints = {
        @UniqueConstraint(columnNames = "hospital")
})
public class HospitalAssignState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String hospital;

    // store last assigned doctor id
    private Long lastDoctorId;
}
