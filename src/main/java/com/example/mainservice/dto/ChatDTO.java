package com.example.mainservice.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatDTO {
    private Long id;
    private String name;
    private String role;
    private String avatar;
    private String lastMessage;
    private String timestamp;
    private Integer unread;
    private Boolean online;
    private List<MessageDTO> messages;
}