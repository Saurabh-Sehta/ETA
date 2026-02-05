package com.Saurabh.ETA.Controller;

import com.Saurabh.ETA.Io.Auth.*;
import com.Saurabh.ETA.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request){
        return authService.createProfile(request);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return authService.getlogin(request);
    }

    @GetMapping("/getUser")
    public ResponseEntity<ProfileResponse> getUser(@CurrentSecurityContext(expression = "authentication.name") String email){
        return authService.getProfile(email);
    }

}
