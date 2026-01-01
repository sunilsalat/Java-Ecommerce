package com.example.demo.auth;

public interface AuthService {

    String register(User user);

    String login(String email, String password);
}
