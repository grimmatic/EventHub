package com.eventhub.eventhub.repository;

import com.eventhub.eventhub.entity.EventMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventMessageRepository extends JpaRepository<EventMessage, Long> {
    List<EventMessage> findByEventId(Long eventId);
}