package com.eventhub.eventhub.controller;

import com.eventhub.eventhub.dto.ChatMessageDTO;
import com.eventhub.eventhub.entity.ChatMessage;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.service.ChatService;
import com.eventhub.eventhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;

    @GetMapping("/sohbet")
    public String chatPage(@RequestParam(required = false) Long userId, Model model) {
        // Mevcut kullanıcı kontrolü
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Kullanıcı bulunamadı");
        }
        model.addAttribute("currentUserId", currentUser.getId());

        // Eğer belirli bir kullanıcıyla sohbet başlatılacaksa
        if (userId != null) {
            try {
                User targetUser = userService.getUserById(userId);
                model.addAttribute("selectedUser", targetUser);
                // Sohbet geçmişini de ekleyelim
                List<ChatMessageDTO> messages = chatService.getUserMessages(userId)
                        .stream()
                        .map(message -> {
                            ChatMessageDTO dto = new ChatMessageDTO(message);
                            dto.setCurrentUser(message.getSender().getId().equals(currentUser.getId()));
                            return dto;
                        })
                        .toList();
                model.addAttribute("initialMessages", messages);
            } catch (Exception e) {
                // Kullanıcı bulunamazsa sessizce devam et
            }
        }

        return "sohbet";
    }

    @GetMapping("/sohbet/messages")
    @ResponseBody
    public List<ChatMessageDTO> getMessages(@RequestParam Long receiverId) {
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Kullanıcı bulunamadı");
        }

        return chatService.getUserMessages(receiverId)
                .stream()
                .map(message -> {
                    ChatMessageDTO dto = new ChatMessageDTO(message);
                    dto.setCurrentUser(message.getSender().getId().equals(currentUser.getId()));
                    return dto;
                })
                .toList();
    }



    // Yeni mesaj göndermek için
    @PostMapping("/sohbet/send")
    @ResponseBody
    public Map<String, Object> sendMessage(@RequestParam String message, @RequestParam Long receiverId) {
        System.out.println("Gönderilen mesaj: " + message); // Log ekleyerek kontrol edin
        try {
            ChatMessage sentMessage = chatService.sendMessage(receiverId, message);
            return Map.of("success", true, "message", sentMessage);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @PostMapping("/sohbet/start")
    @ResponseBody
    public Map<String, Object> startNewChat(@RequestParam Long receiverId) {
        try {
            // İlk mesajı gönderme
            ChatMessage firstMessage = chatService.sendMessage(receiverId, "Yeni sohbet başlatıldı!");
            return Map.of(
                    "success", true,
                    "message", firstMessage
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }

    @DeleteMapping("/sohbet/delete/{messageId}")
    @ResponseBody
    public Map<String, Object> deleteMessage(@PathVariable Long messageId) {
        try {
            chatService.deleteMessage(messageId);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @PutMapping("/sohbet/update/{messageId}")
    @ResponseBody
    public Map<String, Object> updateMessage(@PathVariable Long messageId, @RequestBody Map<String, String> payload) {
        try {
            ChatMessage updatedMessage = chatService.updateMessage(messageId, payload.get("message"));
            return Map.of("success", true, "message", updatedMessage);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

}