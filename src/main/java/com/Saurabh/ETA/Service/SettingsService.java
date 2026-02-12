package com.Saurabh.ETA.Service;

import com.Saurabh.ETA.Entity.EmailEntity;
import com.Saurabh.ETA.Entity.UserEntity;
import com.Saurabh.ETA.Io.Settings.UpdateRequest;
import com.Saurabh.ETA.Repository.EmailRepository;
import com.Saurabh.ETA.Repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UsersRepository usersRepository;
    private final EmailRepository emailRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static Boolean isOtpVerified;

    public ResponseEntity<String> deleteAccount(String email) {
        UserEntity user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Not Authorized"));
        try{
            usersRepository.delete(user);
            return new ResponseEntity<>("Deleted Successfully",HttpStatus.OK);
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<UserEntity> updateProfile(String email, UpdateRequest request) {
        UserEntity user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not Authorized to update"));
        try{
            if (!user.getProfileUrl().equals(request.getProfileUrl()) && !request.getProfileUrl().trim().isEmpty()){
                user.setProfileUrl(request.getProfileUrl());
            }
            if (!user.getFullName().equals(request.getFullName()) && !request.getFullName().trim().isEmpty()){
                user.setFullName(request.getFullName());
            }
            usersRepository.save(user);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
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
