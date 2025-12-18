package com.example.mainservice.repo;

import com.example.mainservice.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByTimestamp(Long conversationId);
}
