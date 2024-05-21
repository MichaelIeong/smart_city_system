package edu.fudan.se.sctap_lowcode_tool.business;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

/**
 * @author ：sunlinyue
 * @date ：Created in 2024/5/21 16:27
 * @description：调用数字空间sdk
 * @modified By：
 * @version: $
 */
public class SdkHandle {

    String projectRoot = Paths.get("").toAbsolutePath().toString();
    String scriptPath = Paths.get(projectRoot, "src", "main", "SDK", "runDeviceControl.js").toString();


    public void setDeviceStatus(String deviceStatus){
        String[] command = getCommand(deviceStatus);
        processCommand(command);
    }

    /**
     * 组装命令
     * @param param
     * @return
     */
    public String[] getCommand(String param){
        String[] command = {
                "node", // Node.js可执行文件
                scriptPath, // Node.js脚本路径
                param // 传递的参数
        };
        return command;
    }

    /**
     * 执行命令
     * @param command
     */
    public void processCommand(String[] command){
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try{
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Node.js script executed successfully.");
            } else {
                System.out.println("Node.js script execution failed with exit code " + exitCode);
            }
        } catch(IOException | InterruptedException e){
            e.printStackTrace();
        }
    }
}
