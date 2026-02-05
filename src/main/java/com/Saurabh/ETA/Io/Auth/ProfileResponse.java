package com.Saurabh.ETA.Io.Auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String profileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
