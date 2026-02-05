package com.Saurabh.ETA.Io.Auth;

import com.Saurabh.ETA.Entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterResponse {
    private Long id;
    private UserEntity user;
    private String token;
}
