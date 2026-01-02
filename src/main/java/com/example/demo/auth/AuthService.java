package com.example.demo.auth;

import java.util.Map;

public interface AuthService {

    String register(User user);

    Map<String, String> login(String email, String password);
}
