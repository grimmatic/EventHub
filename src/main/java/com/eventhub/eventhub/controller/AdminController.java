package com.eventhub.eventhub.controller;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.service.EventService;
import com.eventhub.eventhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final EventService eventService;

    @GetMapping("/panel")
    public String adminPanel(Model model) {
        List<User> users = userService.getAllUsers();
        List<Event> pendingEvents = eventService.getPendingEvents();
        List<Event> allEvents = eventService.getAllEvents();


        model.addAttribute("totalUsers", userService.getTotalUsers());
        model.addAttribute("totalEvents", eventService.getTotalEvents());
        model.addAttribute("activeEvents", eventService.getActiveEvents());
        model.addAttribute("users", users);
        model.addAttribute("pendingEvents", pendingEvents);
        model.addAttribute("allEvents", allEvents);

        return "admin/panel";
    }

    @PostMapping("/event/approve/{id}")
    @ResponseBody
    public Map<String, String> approveEvent(@PathVariable Long id) {
        try {
            eventService.approveEvent(id);
            return Map.of("status", "success", "message", "Etkinlik başarıyla onaylandı");
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    @PostMapping("/event/reject/{id}")
    public String rejectEvent(@PathVariable Long id) {
        eventService.rejectEvent(id);
        return "redirect:/admin/panel";
    }
    @PostMapping("/event/toggle/{id}")
    @ResponseBody
    public Map<String, String> toggleEventStatus(@PathVariable Long id) {
        try {
            Event event = eventService.getEventById(id);
            event.setApproved(!event.getApproved());
            eventService.updateEvent(event);
            return Map.of("status", "success", "message", "Etkinlik durumu güncellendi");
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }


    @DeleteMapping("/event/delete/{id}")
    @ResponseBody
    public Map<String, String> deleteEvent(@PathVariable Long id) {
        try {
            eventService.rejectEvent(id);
            return Map.of("status", "success", "message", "Etkinlik silindi");
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    @PostMapping("/user/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return "redirect:/admin/panel";
    }

    @GetMapping("/reports")
    public String viewReports(Model model) {
        model.addAttribute("totalUsers", userService.getTotalUsers());
        model.addAttribute("totalEvents", eventService.getTotalEvents());
        model.addAttribute("activeEvents", eventService.getActiveEvents());
        return "admin/reports";
    }
}