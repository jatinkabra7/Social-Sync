package com.jk.socialsync.controllers;

import com.jk.socialsync.dtos.requests.LoginRequestDto;
import com.jk.socialsync.dtos.requests.SignupRequestDto;
import com.jk.socialsync.dtos.responses.LoginResponseDto;
import com.jk.socialsync.dtos.responses.SignupResponseDto;
import com.jk.socialsync.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@Valid @RequestBody SignupRequestDto signupRequest) {
        SignupResponseDto signupResponse = authService.signup(signupRequest);
        return new ResponseEntity<>(signupResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        LoginResponseDto loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/refresh")
    public ResponseEntity<LoginResponseDto> login(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.refresh(request, response));
    }
}
