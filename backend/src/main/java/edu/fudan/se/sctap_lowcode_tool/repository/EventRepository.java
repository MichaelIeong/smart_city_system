package edu.fudan.se.sctap_lowcode_tool.repository;


import edu.fudan.se.sctap_lowcode_tool.model.EventInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventInfo, Integer> {

    @Query("SELECT e FROM EventInfo e JOIN e.parentingSpace s WHERE s.projectInfo.projectId = :projectId")
    List<EventInfo> findAllByProjectId(@Param("projectId") Integer projectId);
}
