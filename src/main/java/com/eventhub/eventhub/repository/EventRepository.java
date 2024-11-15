package com.eventhub.eventhub.repository;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCategory(String category);

    @Query("SELECT e FROM Event e WHERE e.approved = true ORDER BY e.startDate DESC")
    List<Event> findAllApprovedEvents();

    @Query(value = "SELECT e.* FROM events e WHERE e.approved = true ORDER BY e.start_date DESC LIMIT 3",
            nativeQuery = true)
    List<Event> findByApprovedIsFalseOrApprovedIsNull();


    long countByApprovedIsTrue();

    List<Event> findByCreatorId(Long creatorId);

    @Query("SELECT e FROM Event e JOIN e.participants p WHERE p = :user")

    List<Event> findByParticipantsUser(User currentUser);

    @Query("SELECT e FROM Event e WHERE e.approved = true ORDER BY e.startDate DESC")
    List<Event> findByApprovedIsTrue();
    Page<Event> findByApprovedIsTrue(Pageable pageable);
    Page<Event> findByApprovedIsTrueAndStartDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<Event> findByApprovedIsTrueAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String nameKeyword, String descriptionKeyword, Pageable pageable);
    Page<Event> findByApprovedIsTrueAndCategoryIn(List<String> categories, Pageable pageable);
    Page<Event> findByApprovedIsTrueAndCategoryInAndStartDateBetween(List<String> categories, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable
    );
}
