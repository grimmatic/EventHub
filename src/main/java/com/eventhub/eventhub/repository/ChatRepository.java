package com.eventhub.eventhub.repository;

import com.eventhub.eventhub.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderIdAndReceiverIdOrderBySentAtDesc(Long senderId, Long receiverId);
    List<ChatMessage> findByReceiverIdOrderBySentAtDesc(Long receiverId);
}