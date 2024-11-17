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

        // İki yönlü mesajlaşmayı getir - hem gönderilen hem alınan
        return chatRepository.findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderBySentAtAsc(
                currentUser.getId(), receiverId,
                receiverId, currentUser.getId()
        );
    }


    public List<ChatMessage> getChatHistory(Long senderId, Long receiverId) {
        return chatRepository.findBySenderIdAndReceiverIdOrderBySentAtDesc(senderId, receiverId);
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

        return chatRepository.save(message);
    }

    public void deleteMessage(Long messageId) {
        ChatMessage message = chatRepository.findById(messageId).orElseThrow(() -> new EntityNotFoundException());
        chatRepository.delete(message);

    }

    public ChatMessage updateMessage(Long messageId, String message) {
        ChatMessage existingMessage = chatRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException());
        existingMessage.setMessageText(message);
        return chatRepository.save(existingMessage);
    }
}