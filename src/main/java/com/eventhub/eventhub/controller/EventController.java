package com.eventhub.eventhub.controller;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.service.EventService;
import com.eventhub.eventhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final UserService userService;

    @GetMapping("/etkinlikler")
    public String listEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(required = false) String specificDate,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        int pageSize = 6;
        Page<Event> eventPage;

        // Arama kontrolü
        if (keyword != null && !keyword.isEmpty()) {
            eventPage = eventService.searchEvents(keyword, page, pageSize);
        }
        // Kategori kontrolü
        else if (category != null && !category.isEmpty()) {
            eventPage = eventService.getEventsByCategory(category, page, pageSize);
        }
        // Tarih filtresi kontrolü
        else if (dateFilter != null || (specificDate != null && !specificDate.isEmpty())) {
            LocalDateTime startDate = null;
            LocalDateTime endDate = LocalDateTime.now().plusYears(100); // Gelecekteki bir tarih

            if (specificDate != null && !specificDate.isEmpty()) {
                startDate = LocalDate.parse(specificDate).atStartOfDay();
                endDate = startDate.plusDays(1);
            } else {
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
                }
            }
            eventPage = eventService.getEventsByDateRange(startDate, endDate, page, pageSize);
        }
        // Filtre yoksa tüm etkinlikleri getir
        else {
            eventPage = eventService.getAllEventsPaged(page, pageSize);
        }

        model.addAttribute("events", eventPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventPage.getTotalPages());
        model.addAttribute("categories", eventService.getAllCategories());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedDateFilter", dateFilter);
        model.addAttribute("specificDate", specificDate);
        model.addAttribute("keyword", keyword);

        return "event/etkinlikler";
    }

    // Etkinlik oluşturma sayfası
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        List<String> categories = eventService.getAllCategories();
        model.addAttribute("categories", categories);
        return "event/create";
    }

    // Etkinlik oluşturma işlemi
    @PostMapping("/create")
    public String createEvent(@ModelAttribute Event event, RedirectAttributes redirectAttributes) {
        try {
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
            List<Event> similarEvents = eventService.getSimilarEvents(id);
            List<User> participants = eventService.getEventParticipants(id);

            model.addAttribute("event", event);
            model.addAttribute("isParticipant", isParticipant);
            model.addAttribute("similarEvents", similarEvents);
            model.addAttribute("participants", participants);

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
            LocalDateTime startDate = LocalDateTime.parse(dates.get("startDate"));
            LocalDateTime endDate = LocalDateTime.parse(dates.get("endDate"));

            boolean hasConflict = eventService.hasDateConflict(startDate, endDate);
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
        List<String> categories = eventService.getAllCategories();

        model.addAttribute("events", events);
        model.addAttribute("categories", categories);
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
}