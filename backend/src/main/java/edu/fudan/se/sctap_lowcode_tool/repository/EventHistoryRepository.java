package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.EventHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventHistoryRepository extends JpaRepository<EventHistory, Integer> {
    @Query("SELECT e FROM EventHistory e " +
            "WHERE e.eventType = :eventType " +
            "AND e.timestamp BETWEEN :start AND CURRENT_TIMESTAMP")
    List<EventHistory> findByEventTypeSince(@Param("eventType") String eventType,
                                            @Param("start") LocalDateTime start);
}
