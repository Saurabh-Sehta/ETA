package com.Saurabh.ETA.Service;

import com.Saurabh.ETA.Entity.EmailEntity;
import com.Saurabh.ETA.Entity.UserEntity;
import com.Saurabh.ETA.Io.Auth.*;
import com.Saurabh.ETA.Repository.EmailRepository;
import com.Saurabh.ETA.Repository.UsersRepository;
import com.Saurabh.ETA.Util.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final EmailRepository emailRepository;
    private final PasswordEncoder passwordEncoder;

    private static boolean isOtpVerified;

    public ResponseEntity<RegisterResponse> createProfile(RegisterRequest request) {
        try{
            if (usersRepository.existsByEmail(request.getEmail())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
            }
            UserEntity user = convertToUserEntity(request);
            usersRepository.save(user);
            emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
            return convertToRegisterResponse(user);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private UserEntity convertToUserEntity(RegisterRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(encoder.encode(request.getPassword()))
                .profileUrl(request.getProfileUrl())
                .build();
    }

    private ResponseEntity<RegisterResponse> convertToRegisterResponse(UserEntity user) {
        final String jwt_token = jwtUtil.generateToken(user.getEmail());
        RegisterResponse response = new RegisterResponse(user.getId(), user, jwt_token);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    public ResponseEntity<LoginResponse> getlogin(LoginRequest request) {
        try{
            authenticate(request.getEmail(), request.getPassword());
            UserEntity user = usersRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserPrincipalNotFoundException("Invalid credentials"));
            final String jwt_token = jwtUtil.generateToken(user.getEmail());
            LoginResponse response = convertToLoginReponse(user, jwt_token);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private LoginResponse convertToLoginReponse(UserEntity user, String jwtToken) {
        return new LoginResponse(user.getId(), user, jwtToken);
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,password));
    }

    public ResponseEntity<ProfileResponse> getProfile(String email) {
        try{
            UserEntity user = usersRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User with this email does not exists."));
            ProfileResponse response = convertToProfileResponse(user);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private ProfileResponse convertToProfileResponse(UserEntity user) {
        return new ProfileResponse(user.getId(), user.getFullName(), user.getEmail(), user.getProfileUrl(), user.getCreatedAt(),user.getUpdatedAt());
    }


    public void sendOtp(String email) {
        if (usersRepository.existsByEmail(email)){
            throw new RuntimeException("User with this email address already exists.");
        }

        EmailEntity user;

        if (emailRepository.existsByEmail(email)) {
            user = emailRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect email address"));
        } else {
            user = EmailEntity.builder()
                    .email(email)
                    .verifyOtp(null)
                    .verifyOtpExpireAt(0L)
                    .otpSentAt(0L)
                    .resetPasswordOtp(0L)
                    .resetPasswordOtpSentAt(0L)
                    .resetPasswordOtpExpiredAt(0L)
                    .build();
        }

        long now = System.currentTimeMillis();

        // Cooldown check (60 seconds)
        if (user.getOtpSentAt() != null && (now - user.getOtpSentAt()) < 60_000) {
            throw new RuntimeException("Please wait before requesting OTP again");
        }

        // Reuse OTP if still valid
        String otp;
        if (user.getVerifyOtp() != null && user.getVerifyOtpExpireAt() > now) {
            otp = user.getVerifyOtp();   // resend same OTP
        } else {
            otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
            user.setVerifyOtp(otp);
            user.setVerifyOtpExpireAt(now + (10 * 60 * 1000)); // 10 minutes
        }
        try {
            // Send email
            emailService.sendOtpEmail(user.getEmail(), otp);

            // Update timestamp & save
            user.setOtpSentAt(now);
            emailRepository.save(user);
        } catch (RuntimeException | MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void verifyOtp(String email, String otp) {
        EmailEntity existingUser;
        if (emailRepository.existsByEmail(email)){
            existingUser = emailRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorect Email"));
            if (existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().equals(otp)){
                throw new RuntimeException("Invalid OTP");
            }
            if (existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis()){
                throw new RuntimeException("OTP Expired");
            }
            existingUser.setIsAccountVerified(true);
            existingUser.setVerifyOtpExpireAt(0L);
            existingUser.setVerifyOtp(null);
            emailRepository.save(existingUser);
        } else {
            throw new RuntimeException("Didn't get this email address");
        }
    }

    public ResponseEntity<String> sendResetOtp(String email) {
        if (!usersRepository.existsByEmail(email)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No user found with given email");
        }
        EmailEntity user = emailRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No user found with given email"));


        try{

            long now = System.currentTimeMillis();

            // Cooldown check (60 seconds)
            if (user.getResetPasswordOtpSentAt() != null && (now - user.getResetPasswordOtpSentAt() < 60_000)) {
                throw new RuntimeException("Please wait before requesting OTP again");
            }

            // Reuse OTP if still valid
            Long otp;
            if (user.getResetPasswordOtp() != null && user.getResetPasswordOtpExpiredAt() > now) {
                otp = user.getResetPasswordOtp();   // resend same OTP
            } else {
                otp = (long) ThreadLocalRandom.current().nextInt(100000, 1000000);
                user.setResetPasswordOtp(otp);
                user.setResetPasswordOtpExpiredAt(now + (10 * 60 * 1000)); // 10 minutes
            }

            emailService.sendResetOtpEmail(user.getEmail(), "Sir/Mam", otp);

            // Update timestamp & save
            user.setResetPasswordOtpSentAt(now);
            emailRepository.save(user);

            return new ResponseEntity<>("OTP has been send to your email address", HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to Send OTP");
        }
    }

    public ResponseEntity<String> verifyResetOtp(String email, String otp) {
        EmailEntity user = emailRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No user found with given email"));
        Long OTP = Long.parseLong(otp);
        if(user.getResetPasswordOtp() == null || !user.getResetPasswordOtp().equals(OTP)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        if (user.getResetPasswordOtpExpiredAt() < System.currentTimeMillis()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP Expired");
        }
        try{
            isOtpVerified = true;
            return new ResponseEntity<>("Verified Successfully", HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to Verify");
        }
    }

    public ResponseEntity<String> ResetPassword(String email, String newPassword) {
        if (!isOtpVerified){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First Verify otp");
        }
        try{
            EmailEntity emailEntity = emailRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No user found with given email"));
            UserEntity user = usersRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No user found with given email"));
            user.setPassword(passwordEncoder.encode(newPassword));
            emailEntity.setResetPasswordOtp(null);
            emailEntity.setResetPasswordOtpExpiredAt(0L);
            usersRepository.save(user);
            emailRepository.save(emailEntity);
            isOtpVerified = false;
            return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to change password");
        }
    }
}
