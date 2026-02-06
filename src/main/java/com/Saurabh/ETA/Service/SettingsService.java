package com.Saurabh.ETA.Service;

import com.Saurabh.ETA.Entity.UserEntity;
import com.Saurabh.ETA.Io.Settings.UpdateRequest;
import com.Saurabh.ETA.Repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UsersRepository usersRepository;

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
}
