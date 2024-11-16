package com.eventhub.eventhub.config;

import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.entity.UserRole;
import com.eventhub.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // Admin kullanıcısı yoksa oluştur
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEmail("admin@eventhub.com");
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setRole(UserRole.ROLE_ADMIN);
            userRepository.save(adminUser);
        }
    }
}