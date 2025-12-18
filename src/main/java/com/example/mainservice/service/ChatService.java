package com.example.mainservice.service;

import com.example.mainservice.model.Message;
import com.example.mainservice.repo.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final MessageRepository messageRepo;

    public ChatService(MessageRepository messageRepo) {
        this.messageRepo = messageRepo;
    }

    public Message saveMessage(Message message) {
        return messageRepo.save(message);
    }

    public List<Message> getMessages(Long conversationId) {
        return messageRepo.findByConversationIdOrderByTimestamp(conversationId);
    }
}
