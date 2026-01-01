package com.example.demo.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = extractTokenFromCookies(request);

        Map<String, Object> claimsMap = new HashMap<String, Object>();

        if (token != null && jwtUtil.validateToken(token)) {
            Claims claims = jwtUtil.decodeToken(token);
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);
            Long id = claims.get("id", Long.class);

            claimsMap.put("email", email);
            claimsMap.put("role", role);
            claimsMap.put("id", id);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    claimsMap, // principal (the user identifier)
                    null, // credentials (we don't need password here)
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}