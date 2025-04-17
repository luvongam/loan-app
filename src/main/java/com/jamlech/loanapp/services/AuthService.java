package com.jamlech.loanapp.services;

import com.jamlech.loanapp.entities.User;
import com.jamlech.loanapp.handlers.AuthResponse;
import com.jamlech.loanapp.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private  final JwtService jwtService;
    private final AuthenticationManager authManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authManager = authManager;
    }
    public AuthResponse register(
            User request
    ){
        User user =new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user=userRepository.save(user);
        String token= jwtService.generateToken(user);
        return  new AuthResponse(token,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
                );
    }
    public  AuthResponse login (
            User request
    ){
        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(()->new UsernameNotFoundException("User not registered"));
        String token= jwtService.generateToken(user);
        return new AuthResponse(token,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
                );

    }
}
