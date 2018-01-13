package com.example.konrad.indoorwayhackathon.net.login;

public class LoginRequest {
    public final String username;
    public final String password;
    public final String grant_type = "password";

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
