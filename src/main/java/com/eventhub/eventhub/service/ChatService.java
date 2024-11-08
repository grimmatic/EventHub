package com.eventhub.eventhub.service;

import com.eventhub.eventhub.entity.ChatMessage;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserService userService;

    public List<ChatMessage> getUserMessages(Long receiverId) {
        return chatRepository.findByReceiverIdOrderBySentAtDesc(receiverId);
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

        return chatRepository.save(message);
    }
}