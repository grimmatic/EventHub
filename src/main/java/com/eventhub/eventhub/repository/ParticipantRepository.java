package com.eventhub.eventhub.repository;

import com.eventhub.eventhub.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByEventId(Long eventId);
    List<Participant> findByUserId(Long userId);
    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @Modifying
    @Query("DELETE FROM Participant p WHERE p.user.id = :userId AND p.event.id = :eventId")
    void deleteByUserIdAndEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.event.creator.id = :organizerId")
    int countParticipantsByOrganizer(@Param("organizerId") Long organizerId);

    @Modifying
    @Query("DELETE FROM Participant p WHERE p.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);
}