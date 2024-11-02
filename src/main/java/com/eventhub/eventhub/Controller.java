package com.eventhub.eventhub;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@org.springframework.stereotype.Controller
public class Controller {

    @GetMapping("/")
    public String AnaSayfa() {
        return "AnaSayfa";
    }

    @GetMapping("/giris")
    public String giris() {
        return "Giris";
    }

    @GetMapping("/kayit")
    public String kayit() {
        return "Kayit";
    }
    @GetMapping("/etkinlikler")
    public String etkinlikler() {
        return "Etkinlikler";
    }

    @GetMapping("/sohbet")
    public String sohbet() {
        return "sohbet";  //
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email, @RequestParam String password) {

        return "redirect:/";
    }

    @PostMapping("/register")
    public String processRegistration(@RequestParam MultipartFile profileImage,
                                      @RequestParam String username,
                                      @RequestParam String password,
                                      @RequestParam String firstName,
                                      @RequestParam String lastName,
                                      @RequestParam LocalDate birthDate,
                                      @RequestParam String gender,
                                      @RequestParam String email,
                                      @RequestParam String phone,
                                      @RequestParam(required = false) List<String> interests) {

        return "redirect:/giris";
    }
}
