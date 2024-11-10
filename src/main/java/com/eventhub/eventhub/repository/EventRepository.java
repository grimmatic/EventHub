package com.eventhub.eventhub.repository;

import com.eventhub.eventhub.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCategory(String category);

    @Query("SELECT e FROM Event e WHERE e.approved = true ORDER BY e.startDate DESC")
    List<Event> findAllApprovedEvents();

    @Query(value = "SELECT e FROM Event e WHERE e.approved = true ORDER BY e.startDate DESC LIMIT 3",
            nativeQuery = true)
    List<Event> findFeaturedEvents();

    List<Event> findByApprovedIsFalseOrApprovedIsNull();
    List<Event> findByApprovedIsTrue();
    long countByApprovedIsTrue();
}