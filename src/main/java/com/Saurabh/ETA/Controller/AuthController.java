package com.Saurabh.ETA.Controller;

import com.Saurabh.ETA.Io.Auth.*;
import com.Saurabh.ETA.Io.Settings.ResetPasswordSendOtpRequest;
import com.Saurabh.ETA.Io.Settings.ResetPaswordRequest;
import com.Saurabh.ETA.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @PostMapping("/send-otp")
    public void sendVerifyOtp(@RequestParam String email){
        try{
            authService.sendOtp(email);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public void verifyEmail(@RequestBody EmailVerifyRequest emailVerifyRequest){
        if (emailVerifyRequest.getOtp() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing details");
        }
        try{
            authService.verifyOtp(emailVerifyRequest.getEmail(), emailVerifyRequest.getOtp());
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/send-reset-otp")
    public ResponseEntity<String> sendResetOtp(@RequestParam String email){
        return authService.sendResetOtp(email);
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<String> verifyResetOtp(@RequestBody ResetPasswordSendOtpRequest request){
        return authService.verifyResetOtp(request.getEmail(), request.getOtp());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPaswordRequest request){
        return authService.ResetPassword(request.getEmail(), request.getNewPassword());
    }

}
