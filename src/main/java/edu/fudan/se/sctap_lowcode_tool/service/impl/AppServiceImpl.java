package edu.fudan.se.sctap_lowcode_tool.service.impl;

import edu.fudan.se.sctap_lowcode_tool.model.AppInfo;
import edu.fudan.se.sctap_lowcode_tool.service.AppService;
import org.springframework.stereotype.Service;

@Service
public class AppServiceImpl implements AppService {
    @Override
    public void saveApp(AppInfo appInfo) {

    }

    @Override
    public AppInfo getInfo(int deviceID) {
        return null;
    }

    @Override
    public void highlightApp(int deviceID) {

    }
}
