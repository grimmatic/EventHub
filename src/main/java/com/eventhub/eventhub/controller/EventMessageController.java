package com.eventhub.eventhub.controller;

import com.eventhub.eventhub.dto.ChatMessageDTO;
import com.eventhub.eventhub.dto.EventMessageDTO;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.service.EventMessageService;
import com.eventhub.eventhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EventMessageController {

    private final UserService userService;
    private final EventMessageService eventMessageService;

    @GetMapping("/event/messages")
    @ResponseBody
    public List<EventMessageDTO> getMessages(@RequestParam Long eventId) {
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Kullanıcı bulunamadı");
        }

        return eventMessageService.getEventMessages(eventId)
                .stream()
                .map(eventMessage -> {
                    EventMessageDTO dto = new EventMessageDTO(eventMessage);
                    dto.setCurrentUser(eventMessage.getSender().getId().equals(currentUser.getId()));
                    return dto;
                })
                .toList();
    }

    @PostMapping("/event/send")
    @ResponseBody
    public EventMessageDTO sendMessage(@RequestParam String message, @RequestParam Long eventId) {
        return new EventMessageDTO(eventMessageService.sendMessage(message, eventId));
    }


}