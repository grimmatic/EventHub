package com.eventhub.eventhub.controller;

import com.eventhub.eventhub.dto.EventMessageDTO;
import com.eventhub.eventhub.entity.ChatMessage;
import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.model.Location;
import com.eventhub.eventhub.service.EventMessageService;
import com.eventhub.eventhub.service.EventService;
import com.eventhub.eventhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.eventhub.eventhub.service.LocationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final UserService userService;
    private final LocationService locationService;
    private final EventMessageService eventMessageService;

    @GetMapping("/etkinlikler")
    public String listEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(required = false) String specificDate,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        int pageSize = 6;
        Page<Event> eventPage;

        // Tarih aralığını hesapla
        LocalDateTime startDate = null;
        LocalDateTime endDate = LocalDateTime.now().plusYears(100);

        if (specificDate != null && !specificDate.isEmpty()) {
            startDate = LocalDate.parse(specificDate).atStartOfDay();
            endDate = startDate.plusDays(1);
        } else if (dateFilter != null) {
            switch (dateFilter) {
                case "today":
                    startDate = LocalDate.now().atStartOfDay();
                    endDate = startDate.plusDays(1);
                    break;
                case "week":
                    startDate = LocalDate.now().atStartOfDay();
                    endDate = startDate.plusWeeks(1);
                    break;
                case "month":
                    startDate = LocalDate.now().atStartOfDay();
                    endDate = startDate.plusMonths(1);
                    break;
                case "all":
                    startDate = LocalDateTime.of(1970, 1, 1, 0, 0);
                    break;
            }
        }

        // Filtreleme mantığı
        if (keyword != null && !keyword.isEmpty()) {
            eventPage = eventService.searchEvents(keyword, page, pageSize);
        } else {
            // Kategori ve tarih filtrelerini birlikte kullan
            eventPage = eventService.getFilteredEvents(category, startDate, endDate, page, pageSize);
        }

        model.addAttribute("events", eventPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventPage.getTotalPages());
        model.addAttribute("categories", eventService.getAllCategories());
        model.addAttribute("selectedCategories", category);
        model.addAttribute("selectedDateFilter", dateFilter);
        model.addAttribute("specificDate", specificDate);
        model.addAttribute("keyword", keyword);

        return "event/etkinlikler";
    }

    // Etkinlik oluşturma sayfası
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        List<String> category = eventService.getAllCategories();
        model.addAttribute("categories", category);
        return "event/create";
    }

    @PostMapping("/create")
    public String createEvent(@ModelAttribute Event event,
                              @RequestParam("latitude") Double latitude,
                              @RequestParam("longitude") Double longitude,
                              RedirectAttributes redirectAttributes) {
        try {
            event.setLatitude(latitude);
            event.setLongitude(longitude);
            eventService.createEvent(event);
            redirectAttributes.addFlashAttribute("success", "Etkinlik başarıyla oluşturuldu! Onay için bekleyiniz.");
            return "redirect:/event/etkinlikler";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/event/create";
        }
    }

    // Tekil etkinlik detay sayfası
    @GetMapping("/{id}")
    public String getEventDetails(@PathVariable Long id, Model model) {
        try {
            Event event = eventService.getEventById(id);
            User currentUser = userService.getCurrentUser();
            boolean isParticipant = eventService.isUserParticipant(id, currentUser.getId());
            boolean isCreator = event.getCreator().getId().equals(currentUser.getId());
            List<Event> similarEvents = eventService.getSimilarEvents(id);
            List<User> participants = eventService.getEventParticipants(id);

            // Zamanla ilgili hesaplamalar
            LocalDateTime now = LocalDateTime.now();
            long daysLeft = ChronoUnit.DAYS.between(now, event.getStartDate());
            long duration = ChronoUnit.HOURS.between(event.getStartDate(), event.getEndDate());

            // Model'e yeni attributelar ekle
            model.addAttribute("daysLeft", Math.max(0, daysLeft));
            model.addAttribute("duration", duration);

            // Mevcut attributelar
            model.addAttribute("event", event);
            model.addAttribute("isParticipant", isParticipant);
            model.addAttribute("isCreator", isCreator);
            model.addAttribute("similarEvents", similarEvents);
            model.addAttribute("participants", participants);
            model.addAttribute("location", locationService.getLocationFromAddress(event));
            model.addAttribute("otherEventLocations", locationService.getOtherEventLocations(id));

            return "event/detail";
        } catch (Exception e) {
            return "redirect:/event/etkinlikler";
        }
    }

    // Etkinliğe katılma işlemi
    @PostMapping("/join/{id}")
    @ResponseBody
    public Map<String, String> joinEvent(@PathVariable Long id) {
        try {
            eventService.joinEvent(id);
            return Map.of(
                    "status", "success",
                    "message", "Etkinliğe başarıyla katıldınız!"
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
        }
    }

    // Etkinlikten ayrılma işlemi
    @PostMapping("/leave/{id}")
    @ResponseBody
    public Map<String, String> leaveEvent(@PathVariable Long id) {
        try {
            eventService.leaveEvent(id);
            return Map.of("status", "success", "message", "Etkinlikten ayrıldınız!");
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    @PostMapping("/check-conflicts")
    @ResponseBody
    public Map<String, Boolean> checkDateConflicts(@RequestBody Map<String, String> dates) {
        try {
            // String tarihleri LocalDateTime'a dönüştür
            LocalDateTime startDate = ZonedDateTime.parse(dates.get("startDate")).toLocalDateTime();
            LocalDateTime endDate = ZonedDateTime.parse(dates.get("endDate")).toLocalDateTime();

            boolean hasConflict = eventService.hasEventDateConflict(startDate, endDate);


            return Map.of("hasConflict", hasConflict);
        } catch (Exception e) {
            return Map.of("hasConflict", true);
        }
    }

    // Filtreleme ve arama işlemleri
    @GetMapping("/search")
    public String searchEvents(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) String location,
                               Model model) {
        List<Event> events = eventService.searchEvents(keyword, category, location);

        model.addAttribute("events", events);
        model.addAttribute("categories", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedLocation", location);

        return "event/etkinlikler";
    }
    @GetMapping("/organizer/{id}")
    public String getOrganizerEvents(@PathVariable Long id, Model model) {
        User organizer = userService.getUserById(id);
        List<Event> events = eventService.getEventsByOrganizer(id);
        int totalParticipants = eventService.getTotalParticipantsByOrganizer(id);

        model.addAttribute("organizer", organizer);
        model.addAttribute("events", events);
        model.addAttribute("eventCount", events.size());
        model.addAttribute("totalParticipants", totalParticipants);

        return "event/organizer-events";
    }
    @GetMapping("/chat-messages")
    @ResponseBody
    public List<EventMessageDTO> getEventMessages(@RequestParam Long eventId) {
        User currentUser = userService.getCurrentUser();
        return eventMessageService.getEventMessages(eventId)
                .stream()
                .map(message -> {
                    EventMessageDTO dto = new EventMessageDTO(message);
                    dto.setCurrentUser(message.getSender().getId().equals(currentUser.getId()));
                    // Profil fotoğrafı URL'sini ekleyin
                    dto.setProfileImageUrl(message.getSender().getProfileImageUrl());
                    return dto;
                })
                .toList();
    }

    @PostMapping("/send-message")
    @ResponseBody
    public Map<String, Object> sendEventMessage(
            @RequestParam String message,
            @RequestParam Long eventId) {
        try {
            ChatMessage sentMessage = eventMessageService.sendMessage(message, eventId);
            User sender = sentMessage.getSender();

            // Yanıt için gerekli verileri hazırla
            Map<String, Object> response = new HashMap<>();
            response.put("id", sentMessage.getId());
            response.put("message", sentMessage.getMessageText());
            response.put("timestamp", sentMessage.getSentAt());
            response.put("firstName", sender.getFirstName());
            response.put("lastName", sender.getLastName());
            response.put("profileImageUrl", sender.getProfileImageUrl());
            response.put("currentUser", true);

            return response;
        } catch (Exception e) {
            return Map.of(
                    "error", true,
                    "message", "Mesaj gönderilemedi: " + e.getMessage()
            );
        }
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public Map<String, String> deleteEvent(@PathVariable Long id) {
        try {
            User currentUser = userService.getCurrentUser();
            Event event = eventService.getEventById(id);

            if (!event.getCreator().getId().equals(currentUser.getId())) {
                return Map.of(
                        "status", "error",
                        "message", "Bu etkinliği silme yetkiniz yok!"
                );
            }

            eventService.deleteEvent(id);
            return Map.of(
                    "status", "success",
                    "message", "Etkinlik başarıyla silindi!"
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
        }
    }

    @GetMapping("/create/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            User currentUser = userService.getCurrentUser();
            Event event = eventService.getEventById(id);

            if (!event.getCreator().getId().equals(currentUser.getId())) {
                return "redirect:/event/etkinlikler";
            }

            List<String> categories = eventService.getAllCategories();
            model.addAttribute("event", event);
            model.addAttribute("categories", categories);
            return "event/create";  // create.html sayfasını kullan
        } catch (Exception e) {
            return "redirect:/event/etkinlikler";
        }
    }

    @PostMapping("/update/{id}")
    public String updateEvent(@PathVariable Long id,
                              @ModelAttribute Event event,
                              @RequestParam("latitude") Double latitude,
                              @RequestParam("longitude") Double longitude,
                              RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();
            Event existingEvent = eventService.getEventById(id);

            if (!existingEvent.getCreator().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "Bu etkinliği düzenleme yetkiniz yok!");
                return "redirect:/event/etkinlikler";
            }

            event.setId(id);
            event.setLatitude(latitude);
            event.setLongitude(longitude);
            event.setCreator(currentUser);
            event.setApproved(existingEvent.getApproved());

            eventService.updateEvent(event);
            redirectAttributes.addFlashAttribute("success", "Etkinlik başarıyla güncellendi!");
            return "redirect:/event/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/event/create/" + id;
        }
    }

}