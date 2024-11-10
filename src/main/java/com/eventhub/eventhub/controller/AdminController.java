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

        model.addAttribute("users", users);
        model.addAttribute("pendingEvents", pendingEvents);
        return "admin/panel";
    }

    @PostMapping("/event/approve/{id}")
    public String approveEvent(@PathVariable Long id) {
        eventService.approveEvent(id);
        return "redirect:/admin/panel";
    }

    @PostMapping("/event/reject/{id}")
    public String rejectEvent(@PathVariable Long id) {
        eventService.rejectEvent(id);
        return "redirect:/admin/panel";
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