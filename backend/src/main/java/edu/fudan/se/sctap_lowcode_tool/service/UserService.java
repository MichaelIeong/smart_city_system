package edu.fudan.se.sctap_lowcode_tool.service;

public interface UserService {

    String login(String username, String password) throws Exception;

    void register(String username, String password) throws Exception;

    int getUserId(String username) throws Exception;
}
