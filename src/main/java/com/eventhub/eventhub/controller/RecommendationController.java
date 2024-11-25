package com.eventhub.eventhub.controller;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.service.RecommendationService;
import com.eventhub.eventhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<Event> getRecommendations() {
        User user = userService.getCurrentUser();

        // Eğer kullanıcı oturum açmamışsa, tüm etkinlikleri döndür
        if (user == null) {
            return recommendationService.getAllEvents(); // Tüm etkinlikleri döndüren metot
        }

        // Kullanıcı oturum açmışsa, kişiselleştirilmiş önerileri döndür
        return recommendationService.getRecommendations(user);
    }


}
