package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.EdgeNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EdgeNodeRepository extends JpaRepository<EdgeNode, Integer> {
    EdgeNode findByGridId(String gridId);
}
