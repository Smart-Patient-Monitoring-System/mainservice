package com.example.mainservice.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private String sender; // "patient", "doctor", "nurse"
    private String text;
    private String time;
    private Boolean read;
    private List<String> attachments;
}