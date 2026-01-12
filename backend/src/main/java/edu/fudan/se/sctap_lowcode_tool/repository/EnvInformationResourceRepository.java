package edu.fudan.se.sctap_lowcode_tool.repository;
import edu.fudan.se.sctap_lowcode_tool.model.EnvInformationResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.EnvSocialResourceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnvInformationResourceRepository extends JpaRepository<EnvInformationResourceInfo, Long> {}
