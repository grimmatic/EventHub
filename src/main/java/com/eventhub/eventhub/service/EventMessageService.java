package com.eventhub.eventhub.service;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.entity.EventMessage;
import com.eventhub.eventhub.repository.EventMessageRepository;
import com.eventhub.eventhub.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventMessageService {
    private final EventMessageRepository eventMessageRepository;
    private final UserService userService;
    private final EventRepository eventRepository;

    public EventMessage sendMessage(String message, Long eventId) {
        EventMessage eventMessage = new EventMessage();
        Event event = eventRepository.findById(eventId).orElseThrow();

        eventMessage.setMessage(message);
        eventMessage.setTimestamp(LocalDateTime.now());
        eventMessage.setSender(userService.getCurrentUser());
        eventMessage.setEvent(event);
        return eventMessageRepository.save(eventMessage);
    }

    public List<EventMessage> getEventMessages(Long eventId) {
        return eventMessageRepository.findByEventId(eventId);
    }

}