package edu.fudan.se.sctap_lowcode_tool.service.impl;

import edu.fudan.se.sctap_lowcode_tool.model.AppInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.AppRepository;
import edu.fudan.se.sctap_lowcode_tool.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppServiceImpl implements AppService {

    @Autowired
    private AppRepository appRepository;

    @Override
    public void saveApp(AppInfo appInfo) {
        appRepository.save(appInfo);
    }

    @Override
    public AppInfo getInfo(int deviceID) {
        return null;
    }

    @Override
    public List<AppInfo> findAllApplication(){
        return appRepository.findAll();
    }

    @Override
    public void deleteAppByName(String appName){
        AppInfo appInfo = appRepository.find
        appRepository.delete();
    }

    @Override
    public void highlightApp(int deviceID) {

    }
}
