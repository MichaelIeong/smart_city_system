package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.PropertyInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<PropertyInfo, Integer> {

    List<PropertyInfo> findByProjectInfoProjectId(Integer projectId);
}
