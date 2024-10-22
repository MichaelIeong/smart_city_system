package edu.fudan.se.sctap_lowcode_tool.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;


@Service
public class NodeRedService {


    // 启动 Node-RED 实例
    public void startNodeRedInstance(int port) throws IOException {
        System.out.println(9999);
        ProcessBuilder processBuilder = new ProcessBuilder("npm", "start", "--", "-p", String.valueOf(port));
        processBuilder.directory(new File("/Users/sunlinyue/LibraryWork/smart_city_system/node-red"));  // Node-RED 目录路径
        processBuilder.start();  // 启动 Node-RED 实例
    }

}
