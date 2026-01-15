package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.TslProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TslProductRepository extends JpaRepository<TslProduct, String> {
}