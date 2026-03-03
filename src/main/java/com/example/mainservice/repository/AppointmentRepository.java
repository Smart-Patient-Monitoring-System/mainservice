package com.example.mainservice.repository;

import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Existing admin method
    List<Appointment> findByPaymentStatus(PaymentStatus paymentStatus);

    // Fetch only successful appointments of a specific patient
    List<Appointment> findByPatientIdAndPaymentStatus(Long patientId, PaymentStatus paymentStatus);
}
