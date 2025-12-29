package com.example.mainservice.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mainservice.model.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByPatientIdOrderByLastMessageTimeDesc(Long patientId);
    Optional<Conversation> findByPatientIdAndProviderId(Long patientId, Long providerId);
}