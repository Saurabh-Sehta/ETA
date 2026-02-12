package com.Saurabh.ETA.Io.Settings;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordSendOtpRequest {

    @Email
    @NotBlank(message = "Email cannot be blank")
    private String email;
    @NotBlank(message = "otp cannot be blank")
    private String otp;

}
