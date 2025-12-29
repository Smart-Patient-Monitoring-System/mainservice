package com.example.mainservice.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long patientId;
    
    @Column(nullable = false)
    private Long providerId; // doctor or nurse ID
    
    @Column(nullable = false)
    private String providerType; // "doctor" or "nurse"
    
    @Column(nullable = false)
    private LocalDateTime lastMessageTime;
    
    @Column(columnDefinition = "TEXT")
    private String lastMessage;
    
    @Column(nullable = false)
    private Integer unreadCount = 0;
}