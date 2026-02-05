package com.Saurabh.ETA.Service;

import com.Saurabh.ETA.Entity.UserEntity;
import com.Saurabh.ETA.Io.Auth.*;
import com.Saurabh.ETA.Repository.UsersRepository;
import com.Saurabh.ETA.Util.JwtUtil;
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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<RegisterResponse> createProfile(RegisterRequest request) {
        try{
            if (usersRepository.existsByEmail(request.getEmail())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
            }
            UserEntity user = convertToUserEntity(request);
            usersRepository.save(user);
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
}
