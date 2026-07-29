package com.appraisal.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.appraisal.dto.AdminLoginRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    public AuthController(
            AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody AdminLoginRequest loginRequest,
            HttpServletRequest request) {

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginRequest.getUsername(),
                                    loginRequest.getPassword()
                            )
                    );

            SecurityContext securityContext =
                    SecurityContextHolder.createEmptyContext();

            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            HttpSession session = request.getSession(true);

            session.setAttribute(
                    HttpSessionSecurityContextRepository
                            .SPRING_SECURITY_CONTEXT_KEY,
                    securityContext
            );

            Map<String, Object> response =
                    new LinkedHashMap<>();

            response.put("authenticated", true);
            response.put("username", authentication.getName());
            response.put("role", "ADMIN");
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException exception) {

            Map<String, Object> response =
                    new LinkedHashMap<>();

            response.put("authenticated", false);
            response.put(
                    "message",
                    "Invalid admin username or password"
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> currentAdmin(
            Authentication authentication) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("authenticated", true);
        response.put("username", authentication.getName());
        response.put("role", "ADMIN");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(
                Map.of("message", "Logout successful")
        );
    }
}