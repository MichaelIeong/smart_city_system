package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.DataSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataSourceTypeRepository extends JpaRepository<DataSourceType, Integer> {
    // 无需额外方法，JpaRepository 已包含 findAll / save 等方法
}
