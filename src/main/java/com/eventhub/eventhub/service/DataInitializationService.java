package com.eventhub.eventhub.service;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.repository.EventRepository;
import com.eventhub.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataInitializationService implements CommandLineRunner {
    private final EventService eventService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Value("${app.init-sample-data:false}")
    private boolean shouldInitializeSampleData;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!shouldInitializeSampleData || eventService.getAllEvents().size() > 0) {
            return;
        }

        User admin = userRepository.findByUsername("admin")
                .orElse(null);

        if (admin == null) {
            return; // Admin kullanıcısı yoksa çık
        }

        // Örnek etkinlikler
        List<Event> sampleEvents = List.of(
                createEvent("Tech Summit 2024",
                        "Teknoloji dünyasının öncüleri ile buluşun ve geleceği şekillendirin. Yapay zeka, blockchain, ve dijital dönüşüm konularında uzman konuşmacılar, interaktif atölyeler ve networking fırsatları.",
                        LocalDateTime.now().plusDays(10),
                        LocalDateTime.now().plusDays(10).plusHours(8),
                        "İstanbul",
                        "Teknoloji",admin),

                createEvent("Rock Festivali",
                        "Açık havada muhteşem bir müzik deneyimi! Ünlü rock grupları, yeni yetenekler ve unutulmaz performanslar.",
                        LocalDateTime.now().plusDays(15),
                        LocalDateTime.now().plusDays(15).plusHours(6),
                        "Ankara",
                        "Müzik",admin),

                createEvent("Yoga ve Meditasyon Atölyesi",
                        "Uzman eğitmenler eşliğinde stres atmak ve içsel huzuru bulmak için yoga ve meditasyon teknikleri.",
                        LocalDateTime.now().plusDays(5),
                        LocalDateTime.now().plusDays(5).plusHours(3),
                        "İzmir",
                        "Spor",admin),

                createEvent("Modern Sanat Sergisi",
                        "Yerel ve uluslararası sanatçıların eserlerinden oluşan kapsamlı bir modern sanat sergisi. Resim, heykel ve enstalasyonlar.",
                        LocalDateTime.now().plusDays(20),
                        LocalDateTime.now().plusDays(25),
                        "İstanbul",
                        "Sanat",admin),


                createEvent("Kariyer Gelişim Zirvesi",
                        "İş dünyasının liderleriyle buluşma fırsatı! CV yazımı, mülakat teknikleri ve kariyer planlaması üzerine oturumlar.",
                        LocalDateTime.now().plusDays(12),
                        LocalDateTime.now().plusDays(12).plusHours(5),
                        "Ankara",
                        "Bilim",admin)
        );


        for (Event event : sampleEvents) {
            eventRepository.save(event);
        }
    }

    private Event createEvent(String name, String description, LocalDateTime startDate,
                              LocalDateTime endDate, String location, String category, User creator) {
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setLocation(location);
        event.setCategory(category);
        event.setApproved(true);
        event.setCreator(creator);
        return event;
    }
}