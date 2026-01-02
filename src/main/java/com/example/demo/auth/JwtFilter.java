package com.example.demo.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    Map<String, List<String>> rolePermissions = Map.of(
            "ADMIN", List.of("USER_VIEW", "USER_CREATE", "USER_UPDATE",
                    "PRODUCT_VIEW", "PRODUCT_CREATE", "PRODUCT_UPDATE", "PRODUCT_DELETE",
                    "ZONE_ENTER", "ZONE_MANAGE"),
            "VENDOR", List.of("PRODUCT_VIEW", "PRODUCT_CREATE", "PRODUCT_UPDATE", "PRODUCT_DELETE",
                    "ZONE_ENTER"),
            "USER", List.of("USER_VIEW", "PRODUCT_VIEW", "ZONE_ENTER"));

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String accessToken = extractCookie(request, "access_token");
        String refreshToken = extractCookie(request, "refresh_token");

        Map<String, Object> claimsMap = new HashMap<String, Object>();
        boolean shouldAuthenticate = false;

        if (accessToken != null && jwtUtil.validateToken(accessToken)) {
            Claims claims = jwtUtil.decodeToken(accessToken);
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);
            Long id = claims.get("id", Long.class);

            claimsMap.put("email", email);
            claimsMap.put("role", role);
            claimsMap.put("id", id);
            shouldAuthenticate = true;
        } else if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
            Claims refreshClaims = jwtUtil.decodeToken(refreshToken);
            String email = refreshClaims.get("email", String.class);
            String role = refreshClaims.get("role", String.class);
            Long id = refreshClaims.get("id", Long.class);

            // Verify user still exists ot not banned or deleted
            User user = userRepository.findByEmail(email)
                    .orElse(null);

            if (user != null && user.getStatus().equals("ACTIVE")) {
                // Generate new access token
                String newAccessToken = jwtUtil.generateAccessToken(user);

                ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", newAccessToken)
                        .httpOnly(true)
                        .path("/")
                        .maxAge(15 * 60) // 15 minutes
                        .sameSite("Lax")
                        .secure(true)
                        .build();

                response.addHeader("Set-Cookie", accessTokenCookie.toString());

                claimsMap.put("email", email);
                claimsMap.put("role", role);
                claimsMap.put("id", id);
                shouldAuthenticate = true;
            }
        }

        if (shouldAuthenticate && !claimsMap.isEmpty()) {
            String role = (String) claimsMap.get("role");
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

            List<String> permissions = rolePermissions.get(role);
            if (permissions != null) {
                permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    claimsMap, // principal (the user identifier)
                    null, // credentials (we don't need password here)
                    authorities // role based permissions
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName()))
                    return cookie.getValue();
            }
        }
        return null;
    }
}