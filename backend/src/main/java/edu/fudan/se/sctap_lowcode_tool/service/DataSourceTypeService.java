package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.DataSourceType;
import edu.fudan.se.sctap_lowcode_tool.repository.DataSourceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataSourceTypeService {

    @Autowired
    private DataSourceTypeRepository repository;

    public List<DataSourceType> listAll() {
        return repository.findAll(); // 直接查询全表
    }

    public DataSourceType create(String name) {
        DataSourceType type = new DataSourceType();
        type.setDatasourceType(name);
        return repository.save(type); // 保存新类型
    }
}
