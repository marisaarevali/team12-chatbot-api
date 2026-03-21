package com.example.bossbot.message.repository;

import com.example.bossbot.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find all active messages for a given conversation, oldest first
     */
    List<Message> findByConversationIdAndIsActiveTrueOrderByCreatedAtAsc(Long conversationId);
}
