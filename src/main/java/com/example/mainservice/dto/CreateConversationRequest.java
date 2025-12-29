package com.example.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequest {
    private Long patientId;
    private Long providerId;
    private String providerType;
}