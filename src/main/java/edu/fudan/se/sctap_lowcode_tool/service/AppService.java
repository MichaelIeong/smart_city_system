package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.AppInfo;


public interface AppService {

    void saveApp(AppInfo appInfo);

    AppInfo getInfo(int deviceID);

    void highlightApp(int deviceID);
}
