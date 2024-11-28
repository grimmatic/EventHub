package com.eventhub.eventhub.service;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.entity.Participant;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.repository.EventRepository;
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

    private static final double EARTH_RADIUS = 6371; // Dünya yarıçapı (kilometre)

    /**
     * Kullanıcıya özel etkinlik önerilerini oluşturur.
     * Önerilen etkinlikleri konuma göre sıralar.
     */
    public List<Event> getRecommendations(User user) {
        if (user.getLatitude() == null || user.getLongitude() == null || user.getInterests() == null) {
            return eventRepository.findAllEvents();
        }
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

        // Önerileri birleştir
        Set<Event> recommendations = new LinkedHashSet<>();
        recommendations.addAll(interestBasedEvents); // İlgi alanına uygun etkinlikler
        recommendations.addAll(filteredRecommendations); // Katıldığı etkinliklerin türündeki öneriler

        // Önerilen etkinlikleri listeye çevir ve konuma göre sırala
        List<Event> sortedRecommendations = new ArrayList<>(recommendations);

        //Eğer hiçbir ilgi alanı ve önceden giidilmiş etkinlik yoksa, tüm etkinlikleri gösteriyoruz
        if(sortedRecommendations.isEmpty()){
            return eventRepository.findAllEvents();
        }

        // Konuma göre sıralama
        double userLat = user.getLatitude();
        double userLon = user.getLongitude();
        sortedRecommendations.sort(Comparator.comparingDouble(event ->
                calculateDistance(userLat, userLon, event.getLatitude(), event.getLongitude())
        ));

        return sortedRecommendations;
    }

    /**
     * Tüm etkinlikleri döner.
     */
    public List<Event> getAllEvents() {
        return eventRepository.findAllEvents();
    }

    /**
     * Haversine formülü ile iki koordinat arasındaki mesafeyi hesaplar.
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // Mesafe kilometre cinsinden
    }
}
