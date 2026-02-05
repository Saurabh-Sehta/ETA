package com.Saurabh.ETA.Io.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterRequest {
    @NotNull(message = "All fields required")
    private String fullName;
    @NotNull(message = "All fields required")
    @Email
    private String email;
    @NotNull(message = "All fields required")
    @Size(min = 8, message = "Password must be 8 characters")
    private String password;
    private String profileUrl;
}
