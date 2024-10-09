package edu.fudan.se.sctap_lowcode_tool.DTO;

public class LoginRequest {
    private String username;
    private String password;

    // Getters and Setters
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

