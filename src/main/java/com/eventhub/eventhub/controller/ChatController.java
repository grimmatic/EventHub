package com.eventhub.eventhub.controller;

import com.eventhub.eventhub.entity.ChatMessage;
import com.eventhub.eventhub.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/sohbet")  // receiverId'yi kaldırdık şimdilik
    public String chatPage(Model model) {
        // Şimdilik boş bir mesaj listesi gönderelim
        model.addAttribute("messages", new ArrayList<>());
        return "sohbet";  // Sadece template adını dönüyoruz, başında "/" yok
    }

    @PostMapping("/sohbet/send")
    @ResponseBody  // AJAX yanıtı için
    public Map<String, Object> sendMessage(@RequestParam String message, @RequestParam Long receiverId) {
        ChatMessage sentMessage = chatService.sendMessage(receiverId, message);
        return Map.of(
                "success", true,
                "message", sentMessage
        );
    }
}
