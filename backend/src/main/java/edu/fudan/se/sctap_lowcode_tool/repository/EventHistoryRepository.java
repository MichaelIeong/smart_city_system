package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.EventHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventHistoryRepository extends JpaRepository<EventHistory, Integer> {
    @Query("SELECT eh FROM EventHistory eh " +
           "JOIN eh.eventInfo ei " +
           "WHERE ei.eventId IN :eventTypeIds " +
           "AND eh.timestamp BETWEEN :fromTime AND :toTime")
    List<EventHistory> findByEventTypeAndTimestamp(
        @Param("eventTypeIds") List<String> eventTypeIds,
        @Param("fromTime") LocalDateTime fromTime,
        @Param("toTime") LocalDateTime toTime
                                                  );

    @Query(value = "SELECT eh.* FROM event_history eh " +
                   "JOIN events ei ON eh.event_id = ei.id " +
                   "WHERE ei.event_id IN (:eventTypeIds) " +
                   "AND eh.timestamp <= :toTime " +
                   "ORDER BY eh.timestamp DESC " +
                   "LIMIT :count", nativeQuery = true)
    List<EventHistory> findTopRecordsBeforeTimeWithLimit(
        @Param("eventTypeIds") List<String> eventTypeIds,
        @Param("toTime") LocalDateTime toTime,
        @Param("count") int count
                                                        );
}
