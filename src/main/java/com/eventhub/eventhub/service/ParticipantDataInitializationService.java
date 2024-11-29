package com.eventhub.eventhub.service;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.entity.Participant;
import com.eventhub.eventhub.entity.User;
import com.eventhub.eventhub.repository.EventRepository;
import com.eventhub.eventhub.repository.ParticipantRepository;
import com.eventhub.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Order(3)
public class ParticipantDataInitializationService implements CommandLineRunner {
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final UserService userService;

    @Value("${app.init-sample-data:false}")
    private boolean shouldInitializeSampleData;

    @Override
    @Transactional
    public void run(String... args) {
        if (!shouldInitializeSampleData || participantRepository.count() > 0) {
            return;
        }

        List<User> users = userRepository.findAll();
        List<Event> events = eventRepository.findAll();
        Random random = new Random();

        users.forEach(user -> {
            if (!"admin".equals(user.getUsername())) { // Admin'i hariç tut
                for (int i = 0; i < 2; i++) {
                    Event randomEvent;
                    do {
                        randomEvent = events.get(random.nextInt(events.size()));
                    } while (participantRepository.existsByUserIdAndEventId(user.getId(), randomEvent.getId()));

                    Participant participant = new Participant();
                    participant.setUser(user);
                    participant.setEvent(randomEvent);
                    participantRepository.save(participant);
                    userService.addPoints(user.getId(), 10, "EVENT_JOIN", randomEvent);
                }
            }
        });
    }
}