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

import java.math.BigDecimal;
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
                        "Torium Avm, 120. Sokak, Turgut Özal Mahallesi, Esenyurt, İstanbul, Marmara Bölgesi, 34513, Türkiye",
                        "Teknoloji",  (41.003894),
                        (28.688275),admin),

                createEvent("Rock Festivali",
                        "Açık havada muhteşem bir müzik deneyimi! Ünlü rock grupları, yeni yetenekler ve unutulmaz performanslar.",
                        LocalDateTime.now().plusDays(15),
                        LocalDateTime.now().plusDays(15).plusHours(6),
                        "Jolly Joker Ankara, 14, Kızılırmak Caddesi, Kavaklıdere Mahallesi, Ankara, Çankaya, Ankara, İç Anadolu Bölgesi, 06420, Türkiye",
                        "Müzik", (39.914637),
                        (32.858265),admin),

                createEvent("Yoga ve Meditasyon Atölyesi",
                        "Uzman eğitmenler eşliğinde stres atmak ve içsel huzuru bulmak için yoga ve meditasyon teknikleri.",
                        LocalDateTime.now().plusDays(5),
                        LocalDateTime.now().plusDays(5).plusHours(3),
                        "Mustafa Kemal Atatürk Karşıyaka Spor Salonu, Cahar Dudayev Bulvarı Yanyolu, Mavişehir Mahallesi, Karşıyaka, İzmir, Ege Bölgesi, 35590, Türkiye",
                        "Spor",(38.474466),
                        (27.076721),admin),

                createEvent("Modern Sanat Sergisi",
                        "Yerel ve uluslararası sanatçıların eserlerinden oluşan kapsamlı bir modern sanat sergisi. Resim, heykel ve enstalasyonlar.",
                        LocalDateTime.now().plusDays(20),
                        LocalDateTime.now().plusDays(25),
                        "Türkan Şoray Kültür Merkezi, 1455. Sokak, Yeşilbahçe Mahallesi, Antalya, Muratpaşa, Antalya, Akdeniz Bölgesi, 07200, Türkiye",
                        "Sanat", (36.869607),
                        (30.724816),admin),


                createEvent("DNA ve Genom Araştırmaları Semineri",
                        "Erzurum'da düzenlenecek bu seminerde, güncel DNA sekanslama teknolojileri ve genom araştırmalarındaki son gelişmeler ele alınacak. Akademisyenler ve araştırmacılar çalışmalarını paylaşacak.",
                        LocalDateTime.now().plusDays(12),
                        LocalDateTime.now().plusDays(12).plusHours(5),
                        "İbrahim Erkal Dadaș Kültür Ve Sanat Merkezi, Yenikapı Sokak, Erzurum, Muratpaşa Mahallesi, Erzurum, Yakutiye, Erzurum, Doğu Anadolu Bölgesi, 25700, Türkiye",
                        "Bilim", (39.905293),
                        (41.271479),admin)
        );


        for (Event event : sampleEvents) {
            eventRepository.save(event);
        }
    }

    private Event createEvent(String name, String description, LocalDateTime startDate,
                              LocalDateTime endDate, String location, String category, Double latitude, Double longitude, User creator) {
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setLocation(location);
        event.setCategory(category);
        event.setLatitude(latitude);
        event.setLongitude(longitude);
        event.setApproved(true);
        event.setCreator(creator);
        return event;
    }
}