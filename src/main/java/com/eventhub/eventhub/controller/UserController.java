package com.eventhub.eventhub.controller;

import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/kayit")
    public String showRegistrationForm() {
        return "user/kayit";
    }

    @PostMapping("/kayit")
    public String kayitOl(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "Kayıt başarılı! Lütfen giriş yapın.");
            return "redirect:/user/giris";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/kayit";
        }
    }

    @GetMapping("/giris")
    public String showLoginForm(Model model) {
        if (model.containsAttribute("success")) {
            model.addAttribute("showSuccess", true);
        }
        return "user/giris";
    }

    @PostMapping("/giris")
    public String girisYap(@RequestParam String username,
                           @RequestParam String password,
                           RedirectAttributes redirectAttributes) {
        if (userService.login(username, password)) {
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", "Geçersiz kullanıcı adı veya şifre");
            return "redirect:/user/giris";
        }
    }

    @GetMapping("/profil")
    public String profil(Model model) {
        try {
            User user = userService.getCurrentUser();
            model.addAttribute("user", user);
            return "user/profil";
        } catch (Exception e) {
            return "redirect:/user/giris";
        }
    }
}