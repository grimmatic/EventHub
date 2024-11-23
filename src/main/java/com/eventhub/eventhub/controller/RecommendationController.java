package com.eventhub.eventhub.controller;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * Kullanıcıya özel etkinlik önerilerini döndüren API.
     *
     * @param user Giriş yapmış kullanıcı (Spring Security'den otomatik alınır)
     * @return Kullanıcıya önerilen etkinliklerin listesi
     */
    @GetMapping
    public List<Event> getRecommendations(@AuthenticationPrincipal User user) {
        if (user == null) {
            throw new IllegalArgumentException("Kullanıcı oturumu mevcut değil.");
        }
        return recommendationService.getRecommendations(user);
    }
}