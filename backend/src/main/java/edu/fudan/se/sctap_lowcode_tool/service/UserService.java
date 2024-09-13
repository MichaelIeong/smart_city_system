package edu.fudan.se.sctap_lowcode_tool.service;

public interface UserService {

    String login(String userName, String passWord) throws Exception;

    void register(String userName, String passWord) throws Exception;
}
