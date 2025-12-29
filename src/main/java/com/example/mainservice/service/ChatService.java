package com.example.mainservice.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mainservice.dto.ChatDTO;
import com.example.mainservice.dto.MessageDTO;
import com.example.mainservice.model.Conversation;
import com.example.mainservice.model.Message;
import com.example.mainservice.model.User;
import com.example.mainservice.repo.ConversationRepository;
import com.example.mainservice.repo.MessageRepository;
import com.example.mainservice.repo.UserRepository;

@Service
public class ChatService {

    private final MessageRepository messageRepo;
    private final ConversationRepository conversationRepo;
    private final UserRepository userRepo;

    public ChatService(MessageRepository messageRepo, 
                       ConversationRepository conversationRepo,
                       UserRepository userRepo) {
        this.messageRepo = messageRepo;
        this.conversationRepo = conversationRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public Message saveMessage(Message message) {
        message.setTimestamp(LocalDateTime.now());
        Message savedMessage = messageRepo.save(message);
        
        // Update conversation
        Conversation conversation = conversationRepo.findById(message.getConversationId())
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        conversation.setLastMessage(message.getText() != null ? message.getText() : "Attachment");
        conversation.setLastMessageTime(message.getTimestamp());
        
        // Increment unread count if sender is not the patient
        if (!"patient".equals(message.getSenderType())) {
            conversation.setUnreadCount(conversation.getUnreadCount() + 1);
        }
        
        conversationRepo.save(conversation);
        
        return savedMessage;
    }

    public List<MessageDTO> getMessages(Long conversationId) {
        List<Message> messages = messageRepo.findByConversationIdOrderByTimestamp(conversationId);
        return messages.stream()
            .map(this::convertToMessageDTO)
            .collect(Collectors.toList());
    }

    public List<ChatDTO> getChatsForPatient(Long patientId) {
        List<Conversation> conversations = conversationRepo.findByPatientIdOrderByLastMessageTimeDesc(patientId);
        
        return conversations.stream()
            .map(conv -> {
                User provider = userRepo.findById(conv.getProviderId())
                    .orElseThrow(() -> new RuntimeException("Provider not found"));
                
                List<Message> messages = messageRepo.findByConversationIdOrderByTimestamp(conv.getId());
                List<MessageDTO> messageDTOs = messages.stream()
                    .map(this::convertToMessageDTO)
                    .collect(Collectors.toList());
                
                ChatDTO chat = new ChatDTO();
                chat.setId(conv.getId());
                chat.setName(provider.getName());
                chat.setRole(provider.getSpecialty());
                chat.setAvatar(provider.getAvatarUrl());
                chat.setLastMessage(conv.getLastMessage());
                chat.setTimestamp(formatTimestamp(conv.getLastMessageTime()));
                chat.setUnread(conv.getUnreadCount());
                chat.setOnline(provider.getOnline());
                chat.setMessages(messageDTOs);
                
                return chat;
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(Long conversationId, Long userId) {
        List<Message> unreadMessages = messageRepo.findByConversationIdAndReadFalse(conversationId);
        
        unreadMessages.stream()
            .filter(msg -> !msg.getSenderId().equals(userId))
            .forEach(msg -> msg.setRead(true));
        
        messageRepo.saveAll(unreadMessages);
        
        // Reset unread count
        Conversation conversation = conversationRepo.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversation.setUnreadCount(0);
        conversationRepo.save(conversation);
    }

    public Conversation createConversation(Long patientId, Long providerId, String providerType) {
        Conversation conversation = new Conversation();
        conversation.setPatientId(patientId);
        conversation.setProviderId(providerId);
        conversation.setProviderType(providerType);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversation.setLastMessage("");
        conversation.setUnreadCount(0);
        
        return conversationRepo.save(conversation);
    }

    private MessageDTO convertToMessageDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSender(message.getSenderType());
        dto.setText(message.getText());
        dto.setTime(formatTimestamp(message.getTimestamp()));
        dto.setRead(message.getRead());
        dto.setAttachments(message.getAttachments());
        return dto;
    }

    private String formatTimestamp(LocalDateTime timestamp) {
        LocalDateTime now = LocalDateTime.now();
        
        if (timestamp.toLocalDate().equals(now.toLocalDate())) {
            return timestamp.format(DateTimeFormatter.ofPattern("h:mm a"));
        } else if (timestamp.toLocalDate().equals(now.minusDays(1).toLocalDate())) {
            return "Yesterday";
        } else {
            return timestamp.format(DateTimeFormatter.ofPattern("MMM d"));
        }
    }
}