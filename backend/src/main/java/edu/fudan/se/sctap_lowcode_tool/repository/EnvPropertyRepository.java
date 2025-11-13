package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.EnvProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvPropertyRepository extends JpaRepository<EnvProperty, Integer> {
    List<EnvProperty> findByGridId(String gridId);
}
