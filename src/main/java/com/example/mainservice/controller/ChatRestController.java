package com.example.mainservice.controller;

import com.example.mainservice.dto.ChatDTO;
import com.example.mainservice.dto.CreateConversationRequest;
import com.example.mainservice.dto.MessageDTO;
import com.example.mainservice.dto.SendMessageRequest;
import com.example.mainservice.model.Conversation;
import com.example.mainservice.model.Message;
import com.example.mainservice.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ChatRestController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatRestController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/conversations/{patientId}")
    public ResponseEntity<List<ChatDTO>> getConversations(@PathVariable Long patientId) {
        return ResponseEntity.ok(chatService.getChatsForPatient(patientId));
    }

    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long conversationId) {
        return ResponseEntity.ok(chatService.getMessages(conversationId));
    }

    @PostMapping("/messages")
    public ResponseEntity<Message> sendMessage(@RequestBody SendMessageRequest request) {
        Message message = new Message();
        message.setConversationId(request.getConversationId());
        message.setSenderId(request.getSenderId());
        message.setSenderType(request.getSenderType());
        message.setText(request.getText());
        message.setAttachments(request.getAttachments());
        message.setRead(false);
        
        Message savedMessage = chatService.saveMessage(message);
        
        // Send via WebSocket
        messagingTemplate.convertAndSend(
            "/topic/conversation/" + request.getConversationId(), 
            savedMessage
        );
        
        return ResponseEntity.ok(savedMessage);
    }

    @PostMapping("/conversations")
    public ResponseEntity<Conversation> createConversation(@RequestBody CreateConversationRequest request) {
        Conversation conversation = chatService.createConversation(
            request.getPatientId(),
            request.getProviderId(),
            request.getProviderType()
        );
        return ResponseEntity.ok(conversation);
    }

    @PutMapping("/messages/read/{conversationId}/{userId}")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long conversationId,
            @PathVariable Long userId) {
        chatService.markMessagesAsRead(conversationId, userId);
        return ResponseEntity.ok().build();
    }
}