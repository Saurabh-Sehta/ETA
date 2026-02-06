package com.Saurabh.ETA.Controller;

import com.Saurabh.ETA.Entity.UserEntity;
import com.Saurabh.ETA.Io.Settings.UpdateRequest;
import com.Saurabh.ETA.Repository.UsersRepository;
import com.Saurabh.ETA.Service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/settings")
public class SettingsController {

    private final SettingsService settingsService;

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAccount(@CurrentSecurityContext(expression = "authentication.name") String email){
        return settingsService.deleteAccount(email);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<UserEntity> updateProfile(
            @CurrentSecurityContext(expression = "authentication.name") String email,
            @Valid @RequestBody UpdateRequest request){
        return settingsService.updateProfile(email, request);
    }

}
