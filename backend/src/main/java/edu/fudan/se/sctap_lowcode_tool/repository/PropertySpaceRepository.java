package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.PropertySpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PropertySpaceRepository extends JpaRepository<PropertySpace, Integer> {
    @Query("SELECT ps.propertyValue " +
            "FROM PropertySpace ps " +
            "JOIN SpaceInfo s ON ps.space = s " +
            "JOIN PropertyInfo p ON ps.property = p " +
            "WHERE s.spaceId = :spaceId " +
            "AND p.propertyId = :propertyId " +
            "AND s.projectInfo.projectId = :projectId")
    String findPropertyValue(@Param("spaceId") String spaceId,
                             @Param("propertyId") String propertyId,
                             @Param("projectId") Integer projectId);
}
