package com.example.demo.auth;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        Map<String, String> tokens = authService.login(user.getEmail(), user.getPassword());

        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", tokens.get("access_token"))
                .httpOnly(true)
                .path("/")
                .maxAge(10 * 60 * 60)
                .sameSite("Lax")
                .secure(true)
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", tokens.get("refresh_token"))
                .httpOnly(true)
                .path("/")
                .maxAge(10 * 60 * 60)
                .sameSite("Lax")
                .secure(true)
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", accessTokenCookie.toString())
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body("Login successful");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .secure(true)
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .secure(true)
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", accessTokenCookie.toString())
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body("Logout successful");
    }
}
