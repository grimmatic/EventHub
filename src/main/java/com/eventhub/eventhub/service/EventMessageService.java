package com.eventhub.eventhub.service;

import com.eventhub.eventhub.entity.ChatMessage;
import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.repository.ChatRepository;
import com.eventhub.eventhub.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventMessageService {
    private final ChatRepository chatRepository;
    private final EventRepository eventRepository;
    private final UserService userService;

    public List<ChatMessage> getEventMessages(Long eventId) {
        return chatRepository.findByEventIdAndIsEventMessageIsTrueOrderBySentAtAsc(eventId);
    }

    public ChatMessage sendMessage(String messageText, Long eventId) {
        User sender = userService.getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı"));

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setEvent(event);
        message.setMessageText(messageText);
        message.setIsEventMessage(true);

        return chatRepository.save(message);
    }
}