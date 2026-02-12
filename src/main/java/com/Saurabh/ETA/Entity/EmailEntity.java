package com.Saurabh.ETA.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
public class EmailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private Boolean isAccountVerified;
    private String verifyOtp;
    private Long verifyOtpExpireAt;
    private Long otpSentAt;
    private Long resetPasswordOtp;
    private Long resetPasswordOtpExpiredAt;
    private Long resetPasswordOtpSentAt;

}
