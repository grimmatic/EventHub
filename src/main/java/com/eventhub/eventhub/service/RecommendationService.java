package com.eventhub.eventhub.service;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.entity.Participant;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.repository.EventRepository;
import com.eventhub.eventhub.service.UserService;
import com.eventhub.eventhub.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    /**
     * Kullanıcıya özel etkinlik önerilerini oluşturur.
     */
    public List<Event> getRecommendations(User user) {

        // Kullanıcının katıldığı etkinliklerin türlerini topla
        List<Participant> participants = participantRepository.findByUserId(user.getId());
        Set<String> attendedCategories = new HashSet<>(); // Katıldığı etkinliklerin kategorileri
        Set<Long> attendedEventIds = new HashSet<>(); // Katıldığı etkinliklerin ID'leri
        for (Participant participant : participants) {
            Event event = participant.getEvent();
            attendedCategories.add(event.getCategory()); // Türlerini ekle
            attendedEventIds.add(event.getId()); // Katıldığı etkinliklerin ID'lerini sakla
        }

        // Kullanıcının katıldığı etkinliklerin türlerinde başka etkinlikler bul
        List<Event> categoryBasedRecommendations = eventRepository.findByApprovedIsTrueAndCategoryIn(
                new ArrayList<>(attendedCategories), Pageable.unpaged()
        ).getContent();

        // Kullanıcının katıldığı etkinlikleri çıkar
        List<Event> filteredRecommendations = new ArrayList<>();
        for (Event event : categoryBasedRecommendations) {
            if (!attendedEventIds.contains(event.getId())) { // Katıldığı etkinlikleri çıkar
                filteredRecommendations.add(event);
            }
        }

        // İlgi Alanı Uyum Kuralı
        List<String> userInterests = Arrays.asList(user.getInterests());
        List<Event> interestBasedEvents = eventRepository.findByApprovedIsTrueAndCategoryIn(userInterests, Pageable.unpaged()).getContent();

        // Önerileri birleştir ve sıralama uygula
        Set<Event> recommendations = new LinkedHashSet<>();
        recommendations.addAll(interestBasedEvents); // İlgi alanına uygun etkinlikler
        recommendations.addAll(filteredRecommendations); // Katıldığı etkinliklerin türündeki öneriler



        return new ArrayList<>(recommendations);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAllEvents();
    }
}
