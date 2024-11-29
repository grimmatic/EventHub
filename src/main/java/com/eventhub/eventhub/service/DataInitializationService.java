package com.eventhub.eventhub.service;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.repository.EventRepository;
import com.eventhub.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Order(2)
public class DataInitializationService implements CommandLineRunner {
    private final EventService eventService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final UserService userService;

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
                        (41.271479),admin),


                createEvent("Gastronomi Festivali",
                        "Türk ve dünya mutfağından en lezzetli yemekler, şef gösterileri ve tadım etkinlikleri ile dolu bir festival.",
                        LocalDateTime.now().plusDays(30),
                        LocalDateTime.now().plusDays(32),
                        "Forum Mersin, Güvenevler Mahallesi, Mersin, Yenişehir, Mersin, Akdeniz Bölgesi, 33140, Türkiye",
                        "Yemek", (36.786762),
                        (34.583772), admin),

                createEvent("Kapadokya Kültür Turu",
                        "Eşsiz peri bacaları, yeraltı şehirleri ve tarihi mekanların keşfi. Balon turu ve yerel el sanatları atölyeleri dahil.",
                        LocalDateTime.now().plusDays(25),
                        LocalDateTime.now().plusDays(27),
                        "Göreme Açık Hava Müzesi, Müze Caddesi, Göreme, Nevşehir, Kapadokya, İç Anadolu Bölgesi, 50180, Türkiye",
                        "Seyahat", (38.643056),
                        (34.828889), admin),

                createEvent("Şiir ve Edebiyat Gecesi",
                        "Ünlü şairler ve yazarlarla buluşma, şiir dinletileri ve imza günü etkinliği.",
                        LocalDateTime.now().plusDays(8),
                        LocalDateTime.now().plusDays(8).plusHours(4),
                        "Nazım Hikmet Kültür Merkezi, Tahran Caddesi, Çankaya, Ankara, İç Anadolu Bölgesi, 06700, Türkiye",
                        "Edebiyat", (39.901860),
                        (32.863970), admin),

                createEvent("E-Spor Turnuvası",
                        "Türkiye'nin en büyük e-spor etkinliği. League of Legends ve Valorant turnuvaları, gaming ekipman fuarı.",
                        LocalDateTime.now().plusDays(45),
                        LocalDateTime.now().plusDays(47),
                        "Volkswagen Arena, Huzur Mahallesi, Sarıyer, İstanbul, Marmara Bölgesi, 34396, Türkiye",
                        "Teknoloji", (41.103734),
                        (29.009540), admin),

                createEvent("Astronomi Gözlem Gecesi",
                        "Profesyonel teleskoplarla gezegen ve yıldız gözlemi, uzman astronomların sunumları.",
                        LocalDateTime.now().plusDays(18),
                        LocalDateTime.now().plusDays(18).plusHours(6),
                        "TÜBİTAK Ulusal Gözlemevi, Sakıp Sabancı Caddesi, Akdeniz Üniversitesi, Antalya, Akdeniz Bölgesi, 07058, Türkiye",
                        "Bilim", (36.824937),
                        (30.335070), admin),

                createEvent("Jazz & Blues Festivali",
                        "Yerli ve yabancı jazz ustaları, blues konserleri ve jam session'lar.",
                        LocalDateTime.now().plusDays(35),
                        LocalDateTime.now().plusDays(37),
                        "Babylon Bomonti, Silahşör Caddesi, Şişli, İstanbul, Marmara Bölgesi, 34381, Türkiye",
                        "Müzik", (41.062137),
                        (28.978830), admin),

                createEvent("Sürdürülebilir Yaşam Fuarı",
                        "Ekolojik yaşam, geri dönüşüm, yenilenebilir enerji ve organik tarım konularında sergiler ve seminerler.",
                        LocalDateTime.now().plusDays(22),
                        LocalDateTime.now().plusDays(24),
                        "Fuar İzmir, Gaziemir, İzmir, Ege Bölgesi, 35410, Türkiye",
                        "Teknoloji", (38.318661),
                        (27.132333), admin),

                createEvent("Geleneksel El Sanatları Festivali",
                        "Anadolu'nun zengin el sanatları kültürünü yaşatan ustalar, canlı demonstrasyonlar ve atölyeler.",
                        LocalDateTime.now().plusDays(28),
                        LocalDateTime.now().plusDays(30),
                        "Eskişehir Büyükşehir Belediyesi Sanat Merkezi, Hoşnudiye Mahallesi, Eskişehir, İç Anadolu Bölgesi, 26130, Türkiye",
                        "Sanat", (39.776667),
                        (30.520556), admin),

                createEvent("Ekstrem Sporlar Festivali",
                        "Paraşüt, kaya tırmanışı, rafting ve wing suit gösterileri. Adrenalin dolu aktiviteler ve yarışmalar.",
                        LocalDateTime.now().plusDays(40),
                        LocalDateTime.now().plusDays(42),
                        "Fethiye Ölüdeniz, Muğla, Ege Bölgesi, 48340, Türkiye",
                        "Spor", (36.549722),
                        (29.124722), admin),

                createEvent("Dünya Mutfakları Workshopu",
                        "İtalyan, Japon, Meksika ve Hint mutfaklarından özel tarifler, pişirme teknikleri ve tadım etkinlikleri.",
                        LocalDateTime.now().plusDays(15),
                        LocalDateTime.now().plusDays(15).plusHours(6),
                        "Mutfak Sanatları Akademisi, Esentepe Mahallesi, Şişli, İstanbul, Marmara Bölgesi, 34394, Türkiye",
                        "Yemek", (41.079306),
                        (29.009722), admin)
        );


        for (Event event : sampleEvents) {
            Event savedEvent = eventRepository.save(event);
            userService.addPoints(admin.getId(), 15, "EVENT_CREATE", savedEvent);
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