package com.example.keklock.common.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {

    @GetMapping("/")
    @ResponseBody
    public Map<String, Object> home(@AuthenticationPrincipal Object principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Social Network API");
        response.put("version", "1.0.0");
        response.put("status", "running");

        if (principal != null) {
            response.put("authenticated", true);
            if (principal instanceof OidcUser) {
                OidcUser user = (OidcUser) principal;
                response.put("user", user.getPreferredUsername());
                response.put("email", user.getEmail());
            } else if (principal instanceof Jwt) {
                Jwt jwt = (Jwt) principal;
                response.put("user", jwt.getSubject());
            }
        } else {
            response.put("authenticated", false);
        }

        response.put("endpoints", Map.of(
            "health", "/actuator/health",
            "register", "/api/auth/register",
            "login", "/oauth2/authorization/keycloak",
            "documentation", "/api-docs"
        ));

        return response;
    }

    @GetMapping("/login-success")
    @ResponseBody
    public Map<String, Object> loginSuccess(@AuthenticationPrincipal OidcUser user) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful!");
        response.put("username", user.getPreferredUsername());
        response.put("email", user.getEmail());
        response.put("name", user.getFullName());

        response.put("nextSteps", Map.of(
            "viewProfile", "/api/profiles/me",
            "createPost", "/api/posts",
            "viewFeed", "/api/posts/feed"
        ));

        return response;
    }

    @GetMapping("/api-info")
    @ResponseBody
    public Map<String, Object> apiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Social Network API");
        info.put("version", "1.0.0");
        info.put("description", "RESTful API for social networking platform");

        Map<String, String> authMethods = new HashMap<>();
        authMethods.put("browser", "Navigate to /oauth2/authorization/keycloak to login via browser");
        authMethods.put("api", "Use POST /realms/my-realm/protocol/openid-connect/token to get JWT token");
        info.put("authentication", authMethods);

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("health", "GET /actuator/health");
        endpoints.put("register", "POST /api/auth/register");
        endpoints.put("profile", "GET /api/profiles/{username}");
        endpoints.put("posts", "GET /api/posts/user/{username}");
        endpoints.put("feed", "GET /api/posts/feed (authenticated)");
        info.put("publicEndpoints", endpoints);

        return info;
    }
}
