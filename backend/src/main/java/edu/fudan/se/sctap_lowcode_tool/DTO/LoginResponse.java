package edu.fudan.se.sctap_lowcode_tool.DTO;

public class LoginResponse {
    private String token;

    private int port;

    public LoginResponse(String token, int port) {
        this.token = token;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
