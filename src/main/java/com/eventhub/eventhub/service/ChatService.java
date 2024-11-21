package com.eventhub.eventhub.service;

import com.eventhub.eventhub.entity.ChatMessage;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.repository.ChatRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserService userService;

    public List<ChatMessage> getUserMessages(Long receiverId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Current user not found");
        }

        return chatRepository.findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderBySentAtAsc(
                currentUser.getId(), receiverId,
                receiverId, currentUser.getId()
        );
    }

    @Transactional
    public ChatMessage sendMessage(Long receiverId, String messageText) {
        User currentUser = userService.getCurrentUser();
        User receiver = userService.getUserById(receiverId);

        ChatMessage message = new ChatMessage();
        message.setSender(currentUser);
        message.setReceiver(receiver);
        message.setMessageText(messageText);
        message.setSentAt(LocalDateTime.now());
        message.setIsEventMessage(false);

        return chatRepository.save(message);
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        ChatMessage message = chatRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        // Sadece mesajı gönderen kişi silebilir
        User currentUser = userService.getCurrentUser();
        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own messages");
        }

        chatRepository.delete(message);
    }

    @Transactional
    public ChatMessage updateMessage(Long messageId, String newMessageText) {
        ChatMessage message = chatRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        // Sadece mesajı gönderen kişi güncelleyebilir
        User currentUser = userService.getCurrentUser();
        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own messages");
        }

        message.setMessageText(newMessageText);
        return chatRepository.save(message);
    }
}