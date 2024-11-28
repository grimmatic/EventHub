package com.eventhub.eventhub.repository;

import com.eventhub.eventhub.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    // Özel mesajlar için
    List<ChatMessage> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderBySentAtAsc(
            Long senderId1, Long receiverId1,
            Long senderId2, Long receiverId2
    );

    // Etkinlik mesajları için
    List<ChatMessage> findByEventIdAndIsEventMessageIsTrueOrderBySentAtAsc(Long eventId);

    // Kullanıcının tüm mesajlarını bulmak için
    List<ChatMessage> findBySenderIdAndReceiverIdOrderBySentAtDesc(Long senderId, Long receiverId);

    // Alıcıya gelen tüm mesajları bulmak için
    List<ChatMessage> findByReceiverIdOrderBySentAtDesc(Long receiverId);
    void deleteByEventIdAndIsEventMessageIsTrue(Long eventId);

}