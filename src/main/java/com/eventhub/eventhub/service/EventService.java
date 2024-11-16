package com.eventhub.eventhub.service;

import com.eventhub.eventhub.entity.*;
import com.eventhub.eventhub.repository.EventRepository;
import com.eventhub.eventhub.repository.ParticipantRepository;
import com.eventhub.eventhub.repository.UserPointsRepository;
import com.eventhub.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserPointsRepository userPointsRepository;

    public List<User> getEventParticipants(Long eventId) {
        return participantRepository.findByEventId(eventId).stream()
                .map(Participant::getUser)
                .collect(Collectors.toList());
    }

    public boolean isUserParticipant(Long eventId, Long userId) {
        return participantRepository.existsByUserIdAndEventId(userId, eventId);
    }
    public List<Event> getApprovedEventsByOrganizer(Long organizerId) {
        return eventRepository.findByCreatorIdAndApprovedIsTrue(organizerId);
    }

    @Transactional
    public Event createEvent(Event event) {
        User creator = userService.getCurrentUser();
        event.setCreator(creator);

        // Admin otomatik onay kontrolü
        event.setApproved(creator.getRole() == UserRole.ROLE_ADMIN);

        if (hasDateConflict(event.getStartDate(), event.getEndDate())) {
            throw new RuntimeException("Bu tarihte başka bir etkinliğiniz bulunmaktadır.");
        }

        Event savedEvent = eventRepository.save(event);

        if (Boolean.TRUE.equals(savedEvent.getApproved())) {
            userService.addPoints(creator.getId(), 15, "EVENT_CREATE", savedEvent);
        }
        return savedEvent;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findByApprovedIsTrue();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı"));
    }
    public List<Event> getPendingEvents() {
        return eventRepository.findAll().stream()
                .filter(event -> !Boolean.TRUE.equals(event.getApproved()))
                .collect(Collectors.toList());
    }

    public Page<Event> searchEvents(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findByApprovedIsTrueAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword, keyword, pageable);
    }


    @Transactional
    public void joinEvent(Long eventId) {
        Event event = getEventById(eventId);
        User currentUser = userService.getCurrentUser();

        if (isUserParticipant(eventId, currentUser.getId())) {
            throw new RuntimeException("Bu etkinliğe zaten katıldınız!");
        }

        if (hasDateConflict(event.getStartDate(), event.getEndDate())) {
            throw new RuntimeException("Bu tarihte başka bir etkinliğiniz bulunmaktadır!");
        }

        // Yeni katılımcı kaydı oluştur
        Participant participant = new Participant();
        participant.setUser(currentUser);
        participant.setEvent(event);
        participantRepository.save(participant);

        // Kullanıcıya puan ekle
        userService.addPoints(currentUser.getId(), 10, "EVENT_JOIN", event);
    }

    @Transactional
    public void leaveEvent(Long eventId) {
        User currentUser = userService.getCurrentUser();
        Event event = getEventById(eventId);

        // Katılımcıyı etkinlikten çıkar
        participantRepository.deleteByUserIdAndEventId(currentUser.getId(), eventId);

        // Puanları geri al (UserService'deki removePoints metodunu kullan)
        userService.removePoints(currentUser.getId(), eventId);
    }

    public boolean hasDateConflict(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            User currentUser = userService.getCurrentUser();
            List<Event> userEvents = eventRepository.findByParticipantsUser(currentUser);
            return userEvents.stream().anyMatch(event ->
                    (startDate.isAfter(event.getStartDate()) && startDate.isBefore(event.getEndDate())) ||
                            (endDate.isAfter(event.getStartDate()) && endDate.isBefore(event.getEndDate())) ||
                            (startDate.isBefore(event.getStartDate()) && endDate.isAfter(event.getEndDate()))
            );
        } catch (RuntimeException e) {
            // Eğer oturum açılmamışsa, çakışma kontrolünü atla
            return false;
        }
    }


    public List<Event> getSimilarEvents(Long eventId) {
        Event event = getEventById(eventId);

        // Aynı kategorideki diğer etkinlikleri getir
        return eventRepository.findByCategory(event.getCategory()).stream()
                .filter(e -> !e.getId().equals(eventId))
                .limit(3)
                .collect(Collectors.toList());
    }
    public Page<Event> getFilteredEvents(List<String> categories, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (categories != null && !categories.isEmpty()) {
            if (startDate != null) {
                // Hem kategori hem de tarih filtresi var
                return eventRepository.findByApprovedIsTrueAndCategoryInAndStartDateBetween(
                        categories, startDate, endDate, pageable);
            } else {
                // Sadece kategori filtresi var
                return eventRepository.findByApprovedIsTrueAndCategoryIn(categories, pageable);
            }
        } else if (startDate != null) {
            // Sadece tarih filtresi var
            return eventRepository.findByApprovedIsTrueAndStartDateBetween(startDate, endDate, pageable);
        } else {
            // Hiç filtre yok
            return eventRepository.findByApprovedIsTrue(pageable);
        }
    }


    public List<Event> searchEvents(String keyword, String category, String location) {
        if (keyword == null && category == null && location == null) {
            return getAllEvents();
        }

        return eventRepository.findAllApprovedEvents().stream()
                .filter(event ->
                        (keyword == null || event.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                                event.getDescription().toLowerCase().contains(keyword.toLowerCase())) &&
                                (category == null || event.getCategory().equals(category)) &&
                                (location == null || event.getLocation().toLowerCase().contains(location.toLowerCase()))
                )
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveEvent(Long eventId) {
        Event event = getEventById(eventId);

        if (Boolean.TRUE.equals(event.getApproved())) {
            return;
        }

        event.setApproved(true);
        eventRepository.save(event);


            userService.addPoints(event.getCreator().getId(), 15, "EVENT_CREATE", event);

    }

    @Transactional
    public void rejectEvent(Long eventId) {
        Event event = getEventById(eventId);

        // Eğer etkinlik zaten onaylıysa, puanları geri al
        if (Boolean.TRUE.equals(event.getApproved())) {
            try {
                // EVENT_CREATE puanlarını bul ve sil
                List<UserPoints> createPoints = userPointsRepository.findByUserIdAndEventIdAndActivityType(
                        event.getCreator().getId(),
                        eventId,
                        "EVENT_CREATE"
                );

                if (!createPoints.isEmpty()) {
                    User creator = event.getCreator();
                    UserPoints points = createPoints.get(0);
                    creator.setTotalPoints(creator.getTotalPoints() - points.getPoints());
                    userRepository.save(creator);
                    userPointsRepository.delete(points);
                }

                // Katılımcıların puanlarını geri al
                List<Participant> participants = participantRepository.findByEventId(eventId);
                for (Participant participant : participants) {
                    userService.removePoints(participant.getUser().getId(), eventId);
                }
            } catch (Exception e) {

            }
        }

        // Katılımcıları sil
        participantRepository.deleteByEventId(eventId);

        // Etkinliği sil
        eventRepository.deleteById(eventId);
    }

    @Transactional
    public Event updateEvent(Event event) {
        return eventRepository.save(event);
    }

    public long getTotalEvents() {
        return eventRepository.count();
    }

    public long getActiveEvents() {
        return eventRepository.findByApprovedIsTrue().size();
    }

    public List<String> getAllCategories() {
        return List.of("Müzik", "Spor", "Teknoloji", "Sanat", "Bilim", "Yemek" ,"Seyahat", "Edebiyat");
    }
    public List<Event> getEventsByOrganizer(Long organizerId) {
        return eventRepository.findByCreatorId(organizerId);
    }

    public int getTotalParticipantsByOrganizer(Long organizerId) {
        return participantRepository.countParticipantsByOrganizer(organizerId);
    }

}