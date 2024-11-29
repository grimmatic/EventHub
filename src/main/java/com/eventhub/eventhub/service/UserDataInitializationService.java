package com.eventhub.eventhub.service;

import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.entity.UserRole;
import com.eventhub.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Order(1)
public class UserDataInitializationService implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init-sample-data:false}")
    private boolean shouldInitializeSampleData;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (!shouldInitializeSampleData || userRepository.count() > 1) {
            return;
        }

        List<User> sampleUsers = List.of(
                createUser("ahmet.yilmaz", "Ahmet", "Yılmaz", "ahmet.yilmaz@gmail.com",
                        LocalDate.of(1990, 5, 15), "Erkek", "5551234567",
                        new String[]{"Teknoloji", "Spor", "Müzik"}, 41.0082, 28.9784),

                createUser("zeynep.kaya", "Zeynep", "Kaya", "zeynep.kaya@gmail.com",
                        LocalDate.of(1988, 8, 22), "Kadın", "5559876543",
                        new String[]{"Sanat", "Edebiyat", "Yemek"}, 39.9334, 32.8597),

                createUser("mehmet.demir", "Mehmet", "Demir", "mehmet.demir@gmail.com",
                        LocalDate.of(1995, 3, 10), "Erkek", "5553456789",
                        new String[]{"Spor", "Teknoloji", "Seyahat"}, 38.4192, 27.1287),

                createUser("ayse.sahin", "Ayşe", "Şahin", "ayse.sahin@gmail.com",
                        LocalDate.of(1992, 11, 28), "Kadın", "5557891234",
                        new String[]{"Bilim", "Müzik", "Yemek"}, 36.8969, 30.7133),

                createUser("can.ozturk", "Can", "Öztürk", "can.ozturk@gmail.com",
                        LocalDate.of(1993, 7, 4), "Erkek", "5552345678",
                        new String[]{"Teknoloji", "Bilim", "Spor"}, 41.1621, 29.0611),

                createUser("elif.arslan", "Elif", "Arslan", "elif.arslan@gmail.com",
                        LocalDate.of(1991, 9, 17), "Kadın", "5558765432",
                        new String[]{"Sanat", "Seyahat", "Müzik"}, 40.1885, 29.0610),

                createUser("baris.yildiz", "Barış", "Yıldız", "baris.yildiz@gmail.com",
                        LocalDate.of(1994, 4, 30), "Erkek", "5554567890",
                        new String[]{"Edebiyat", "Spor", "Teknoloji"}, 37.0367, 37.3833)
        );

        for (User user : sampleUsers) {
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole(UserRole.ROLE_USER);
            user.setEnabled(true);
            user.setTotalPoints(0);
            userRepository.save(user);
        }
    }

    private User createUser(String username, String firstName, String lastName, String email,
                            LocalDate birthDate, String gender, String phone, String[] interests,
                            Double latitude, Double longitude) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setBirthDate(birthDate);
        user.setGender(gender);
        user.setPhone(phone);
        user.setInterests(interests);
        user.setLatitude(latitude);
        user.setLongitude(longitude);
        user.setProfileImageUrl("/images/default-avatar.png");
        return user;
    }
}