package edu.fudan.se.sctap_lowcode_tool.repository;


import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface DeviceRepository extends Mapper<DeviceInfo>{

}
