// HospitalAssignStateRepo.java
package com.example.mainservice.repository;

import com.example.mainservice.entity.HospitalAssignState;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface HospitalAssignStateRepo extends JpaRepository<HospitalAssignState, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM HospitalAssignState s WHERE s.hospital = :hospital")
    Optional<HospitalAssignState> findByHospitalForUpdate(@Param("hospital") String hospital);

    Optional<HospitalAssignState> findByHospital(String hospital);
}
