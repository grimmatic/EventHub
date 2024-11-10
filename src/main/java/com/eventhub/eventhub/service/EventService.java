package com.eventhub.eventhub.service;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserService userService;

    public List<Event> getAllEvents() {
        return eventRepository.findAllApprovedEvents();
    }

    public List<Event> getFeaturedEvents() {
        return eventRepository.findFeaturedEvents();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı"));
    }
    public List<Event> getPendingEvents() {
        return eventRepository.findByApprovedIsFalseOrApprovedIsNull();
    }


    @Transactional
    public Event createEvent(Event event) {
        event.setCreator(userService.getCurrentUser());
        event.setApproved(false); // Admin onayı bekleyecek
        return eventRepository.save(event);
    }
    @Transactional
    public void approveEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı"));
        event.setApproved(true);
        eventRepository.save(event);
    }

    // Etkinliği reddet/sil
    @Transactional
    public void rejectEvent(Long eventId) {
        eventRepository.deleteById(eventId);
    }

    // Toplam etkinlik sayısı
    public long getTotalEvents() {
        return eventRepository.count();
    }

    // Aktif (onaylanmış) etkinlik sayısı
    public long getActiveEvents() {
        return eventRepository.countByApprovedIsTrue();
    }

    public Map<String, Integer> getStats() {
        return Map.of(
                "totalEvents", (int) eventRepository.count(),
                "activeEvents", eventRepository.findAllApprovedEvents().size(),
                "categories", 4, // Sabit değer yerine gerçek kategori sayısı alınabilir
                "cities", 50    // Sabit değer yerine gerçek şehir sayısı alınabilir
        );
    }

    public List<String> getAllCategories() {
        return List.of("TECHNOLOGY", "MUSIC", "SPORTS", "ART", "EDUCATION", "SOCIAL");
    }
}