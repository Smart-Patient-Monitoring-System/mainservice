package com.example.mainservice.repository;

import com.example.mainservice.entity.LoginOtpSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginOtpSessionRepository extends JpaRepository<LoginOtpSession, Long> {
    Optional<LoginOtpSession> findBySessionId(String sessionId);
    void deleteByPatientId(Long patientId);
}

