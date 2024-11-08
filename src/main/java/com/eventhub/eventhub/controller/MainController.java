package com.eventhub.eventhub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;

@Controller
public class MainController {

    @GetMapping("/")
    public String home() {
        return "anaSayfa";
    }

    @GetMapping("/giris")
    public String giris() {
        return "user/giris";
    }

    @GetMapping("/kayit")
    public String kayit() {
        return "user/kayit";
    }

    @GetMapping("/etkinlikler")
    public String etkinlikler(Model model) {
            return "event/etkinlikler";

    }
}