package com.jamlech.loanapp.controllers;


import com.jamlech.loanapp.entities.User;
import com.jamlech.loanapp.handlers.AuthResponse;
import com.jamlech.loanapp.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody User request
    ){
        return ResponseEntity.ok(
                authService.register(request)
        );
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody User request
    ){
        return ResponseEntity.ok(
                authService.login(request)
        );
    }
}
