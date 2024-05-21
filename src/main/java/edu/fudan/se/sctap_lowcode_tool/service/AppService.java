package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.AppInfo;

import java.util.List;


public interface AppService {

    void saveApp(AppInfo appInfo);

    AppInfo getInfo(int deviceID);

    List<AppInfo> findAllApplication();

    void deleteAppByName(String appName);

    void highlightApp(int deviceID);
}
