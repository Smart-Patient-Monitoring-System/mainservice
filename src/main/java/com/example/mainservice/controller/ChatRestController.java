package com.example.mainservice.controller;

import com.example.mainservice.model.Message;
import com.example.mainservice.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/messages/{conversationId}")
    public List<Message> getMessages(@PathVariable Long conversationId) {
        return chatService.getMessages(conversationId);
    }

    @PostMapping("/send")
    public Message sendMessage(@RequestBody Message message) {
        return chatService.saveMessage(message);
    }
}
