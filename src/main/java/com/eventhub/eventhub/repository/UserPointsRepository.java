package com.eventhub.eventhub.repository;

import com.eventhub.eventhub.entity.UserPoints;
import com.eventhub.eventhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserPointsRepository extends JpaRepository<UserPoints, Long> {
    List<UserPoints> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(up.points), 0) FROM UserPoints up WHERE up.user.id = :userId")
    Integer getTotalPointsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(up) > 0 FROM UserPoints up WHERE up.user.id = :userId AND up.activityType = 'FIRST_EVENT'")
    boolean hasFirstEventBonus(@Param("userId") Long userId);
}
