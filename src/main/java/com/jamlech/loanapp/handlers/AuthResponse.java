package com.jamlech.loanapp.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthResponse {
    private String token;
    private String email;
    private String firstName;
    private String lastName;
}
