package com.jk.socialsync.services;

import com.jk.socialsync.dtos.requests.LoginRequestDto;
import com.jk.socialsync.dtos.requests.SignupRequestDto;
import com.jk.socialsync.dtos.responses.LoginResponseDto;
import com.jk.socialsync.dtos.responses.SignupResponseDto;
import com.jk.socialsync.entities.UserEntity;
import com.jk.socialsync.repositories.UserRepository;
import com.jk.socialsync.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public SignupResponseDto signup(SignupRequestDto signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

        UserEntity user = UserEntity.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .passwordHash(passwordEncoder.encode(signupRequest.getPassword()))
                .build();

        userRepository.save(user);

        String jwt = jwtUtil.generateToken(user);

        return SignupResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .jwt(jwt)
                .build();
    }

    public LoginResponseDto login(LoginRequestDto loginRequest) {
        if (!userRepository.existsByUsername(loginRequest.getUsername())) {
            throw new IllegalArgumentException("Account not found, please register with us");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        UserEntity user = (UserEntity) authentication.getPrincipal();

        String jwt = jwtUtil.generateToken(user);

        return LoginResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .jwt(jwt)
                .build();
    }
}
