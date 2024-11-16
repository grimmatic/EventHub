package com.eventhub.eventhub.service;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.entity.UserPoints;
import com.eventhub.eventhub.entity.UserRole;
import com.eventhub.eventhub.repository.UserPointsRepository;
import com.eventhub.eventhub.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserPointsRepository userPointsRepository;
    @Value("${file.upload-dir:uploads/profiles}")
    private String uploadDir;

    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Bu email zaten kullanımda");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Bu kullanıcı adı zaten kullanımda");
        }

        // Şifreyi hashle
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        // Varsayılan rol ata
        user.setRole(UserRole.valueOf("ROLE_USER"));

        if (user.getInterests() == null && user.getInterests().length > 0) {
            user.setInterests(new String[0]);

        }

        // Profil fotoğrafını işle
        MultipartFile profileImage = user.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" +
                        StringUtils.cleanPath(profileImage.getOriginalFilename());
                Path uploadPath = Path.of("src/main/resources/static/uploads/profiles");
                Files.createDirectories(uploadPath);
                Path destinationFile = uploadPath.resolve(fileName);

                // Dosyayı kopyala
                try (InputStream inputStream = profileImage.getInputStream()) {
                    Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                }

                // URL'yi kaydet
                user.setProfileImageUrl("/uploads/profiles/" + fileName);
            } catch (IOException e) {
                throw new RuntimeException("Profil fotoğrafı kaydedilemedi: " + e.getMessage());
            }
        } else {
            // Varsayılan profil fotoğrafı
            user.setProfileImageUrl("/images/default-avatar.png");
        }

        // Kullanıcı kaydını gerçekleştir
        return userRepository.save(user);
    }


    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Path.of(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));
    }
    public boolean login(String username, String rawPassword) {
        try {
            // Veritabanından kullanıcıyı bul
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            // Authentication işlemini gerçekleştir
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, rawPassword)
            );

            // Başarılı authentication sonrası security context'i güncelle
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return true;
        } catch (AuthenticationException e) {
            return false;
        }
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        }
        throw new RuntimeException("Oturum açılmamış");
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    @Transactional
    public void updateUser(User updatedUser, MultipartFile profileImage) {
        User currentUser = getCurrentUser();

        // Null kontrolü ile güvenli güncelleme
        if (StringUtils.hasText(updatedUser.getFirstName())) {
            currentUser.setFirstName(updatedUser.getFirstName());
        }
        if (StringUtils.hasText(updatedUser.getLastName())) {
            currentUser.setLastName(updatedUser.getLastName());
        }
        if (StringUtils.hasText(updatedUser.getEmail())) {
            // Email değişiyorsa, benzersiz olduğunu kontrol et
            if (!currentUser.getEmail().equals(updatedUser.getEmail()) &&
                    userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new RuntimeException("Bu email adresi zaten kullanımda");
            }
            currentUser.setEmail(updatedUser.getEmail());
        }
        if (StringUtils.hasText(updatedUser.getPhone())) {
            currentUser.setPhone(updatedUser.getPhone());
        }

        currentUser.setInterests(updatedUser.getInterests());

        // Profil fotoğrafını işle
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // Eski profil fotoğrafını sil (varsayılan avatar değilse)
                if (currentUser.getProfileImageUrl() != null &&
                        !currentUser.getProfileImageUrl().equals("/images/default-avatar.png")) {
                    Path oldImagePath = Path.of("src/main/resources/static" + currentUser.getProfileImageUrl());
                    Files.deleteIfExists(oldImagePath);
                }

                String fileName = UUID.randomUUID().toString() + "_" +
                        StringUtils.cleanPath(profileImage.getOriginalFilename());

                Path uploadPath = Path.of("src/main/resources/static/uploads/profiles");
                Files.createDirectories(uploadPath);
                Path destinationFile = uploadPath.resolve(fileName);

                Files.copy(profileImage.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                currentUser.setProfileImageUrl("/uploads/profiles/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("Profil fotoğrafı yüklenirken hata oluştu: " + e.getMessage());
            }
        }

        try {
            userRepository.save(currentUser);
        } catch (Exception e) {
            throw new RuntimeException("Profil güncellenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Eğer User entity'sine enabled field'ı eklerseniz:
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    // Toplam kullanıcı sayısı
    public long getTotalUsers() {
        return userRepository.count();
    }



    @Transactional
    public void addPoints(Long userId, Integer points, String activityType, Event event) {
        User user = getUserById(userId);
        List<UserPoints> existingPoints = userPointsRepository.findByUserIdAndEventIdAndActivityType(
                userId, event.getId(), activityType
        );
        if (!existingPoints.isEmpty()) {
            return;
        }
        if (event != null && !Boolean.TRUE.equals(event.getApproved())) {
            return;
        }

        // İlk etkinlik katılımı kontrolü
        if ("EVENT_JOIN".equals(activityType) && !userPointsRepository.hasFirstEventBonus(userId)) {
            UserPoints firstEventBonus = new UserPoints();
            firstEventBonus.setUser(user);
            firstEventBonus.setPoints(20);
            firstEventBonus.setActivityType("FIRST_EVENT_BONUS");
            firstEventBonus.setEvent(event);
            userPointsRepository.save(firstEventBonus);

            user.setTotalPoints(user.getTotalPoints() + 20);
        }

        // Normal puan ekleme
        UserPoints userPoints = new UserPoints();
        userPoints.setUser(user);
        userPoints.setPoints(points);
        userPoints.setActivityType(activityType);
        userPoints.setEvent(event);
        userPointsRepository.save(userPoints);

        user.setTotalPoints(user.getTotalPoints() + points);
        userRepository.save(user);
    }
    @Transactional
    public void removePoints(Long userId, Long eventId) {
        User user = getUserById(userId);

        // Etkinliğe katılım için verilen puanları bul
        List<UserPoints> eventJoinPoints = userPointsRepository.findByUserIdAndEventIdAndActivityType(
                userId, eventId, "EVENT_JOIN"
        );

        if (!eventJoinPoints.isEmpty()) {
            UserPoints joinPoints = eventJoinPoints.get(0);
            // Katılım puanını geri al (10 puan)
            user.setTotalPoints(user.getTotalPoints() - joinPoints.getPoints());
            userPointsRepository.delete(joinPoints);
        }

        // İlk katılım bonusu puanlarını bul
        List<UserPoints> firstEventBonusPoints = userPointsRepository.findByUserIdAndEventIdAndActivityType(
                userId, eventId, "FIRST_EVENT_BONUS"
        );

        if (!firstEventBonusPoints.isEmpty()) {
            UserPoints bonusPoints = firstEventBonusPoints.get(0);
            // İlk katılım puanını geri al (20 puan)
            user.setTotalPoints(user.getTotalPoints() - bonusPoints.getPoints());
            userPointsRepository.delete(bonusPoints);
        }

        userRepository.save(user);
    }


}