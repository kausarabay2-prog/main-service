package com.bitlab.main_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/me")
@Tag(name = "Me", description = "The current user taken from the Keycloak token")
public class MeController {

    @GetMapping
    @Operation(summary = "Get the current user and its authorities (GrantedAuthority)")
    public Map<String, Object> me(@AuthenticationPrincipal User user) {
        List<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();
        return Map.of("username", user.getUsername(), "authorities", authorities );

    }}