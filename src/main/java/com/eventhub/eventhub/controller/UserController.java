package com.eventhub.eventhub.controller;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.service.EventService;
import com.eventhub.eventhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final EventService eventService;

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
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                model.addAttribute("profileImage", user.getProfileImageUrl());
            } else {
                model.addAttribute("profileImage", "/images/default-avatar.png");
            }


            List<Event> userEvents = eventService.getApprovedEventsByOrganizer(user.getId());
            model.addAttribute("userEvents", userEvents);
            model.addAttribute("user", user);
            return "user/profil";
        } catch (Exception e) {
            return "redirect:/user/giris";
        }
    }

    @GetMapping("/profil-duzenle")
    public String profilDuzenle(Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getCurrentUser();
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Kullanıcı bulunamadı");
                return "redirect:/user/giris";
            }

            if (user.getInterests() == null) {
                user.setInterests(new String[0]);
            }


            // İlgi alanlarını model'e ekle
            model.addAttribute("allInterests", Arrays.asList(
                    "Müzik", "Spor", "Teknoloji", "Sanat",
                    "Bilim", "Yemek", "Seyahat", "Edebiyat"
            ));
            model.addAttribute("user", user);
            return "user/profil-duzenle";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Oturum süreniz dolmuş olabilir. Lütfen tekrar giriş yapın.");
            return "redirect:/user/giris";
        }
    }

    @PostMapping("/profil-duzenle")
    public String profilGuncelle(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam(value = "interests", required = false) String[] interests,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            RedirectAttributes redirectAttributes) {

        try {
            // Mevcut kullanıcıyı al
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                throw new RuntimeException("Kullanıcı bulunamadı");
            }

            // Kullanıcı bilgilerini güncelle
            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            currentUser.setEmail(email);
            currentUser.setPhone(phone);
            currentUser.setInterests(interests != null ? interests : new String[0]);

            // Konum bilgilerini güncelle
            if (latitude != null && longitude != null) {
                currentUser.setLatitude(latitude);
                currentUser.setLongitude(longitude);
            }

            // Kullanıcıyı güncelle
            userService.updateUser(currentUser, profileImage);
            redirectAttributes.addFlashAttribute("success", "Profiliniz başarıyla güncellendi.");
            return "redirect:/user/profil";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/profil-duzenle";
        }
    }


    @GetMapping("/users/list")
    @ResponseBody
    public List<Map<String, Object>> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("firstName", user.getFirstName());
                    map.put("lastName", user.getLastName());
                    map.put("profileImageUrl", user.getProfileImageUrl() != null ?
                            user.getProfileImageUrl() : "/images/default-avatar.png");
                    map.put("online", false);
                    return map;
                })
                .toList();
    }

    @GetMapping("/{userId}/profil")
    public String userProfile(@PathVariable Long userId, Model model) {
        try {
            User user = userService.getUserById(userId);
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                model.addAttribute("profileImage", user.getProfileImageUrl());
            } else {
                model.addAttribute("profileImage", "/images/default-avatar.png");
            }

            List<Event> userEvents = eventService.getApprovedEventsByOrganizer(userId);
            model.addAttribute("userEvents", userEvents);
            model.addAttribute("user", user);
            return "user/profil";
        } catch (Exception e) {
            return "redirect:/";
        }
    }
    @GetMapping("/sifremi-unuttum")
    public String showForgotPasswordForm() {
        return "user/sifremi-unuttum";
    }

    @PostMapping("/sifremi-unuttum")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            userService.initiatePasswordReset(email);
            redirectAttributes.addFlashAttribute("success",
                    "Şifre sıfırlama bağlantısı email adresinize gönderildi");
            return "redirect:/user/giris";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/sifremi-unuttum";
        }
    }

    @GetMapping("/sifre-sifirla")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "user/sifre-sifirla";
    }

    @PostMapping("/sifre-sifirla")
    public String processResetPassword(
            @RequestParam String token,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(token, password);
            redirectAttributes.addFlashAttribute("success", "Şifreniz başarıyla güncellendi");
            return "redirect:/user/giris";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/sifre-sifirla?token=" + token;
        }
    }
}