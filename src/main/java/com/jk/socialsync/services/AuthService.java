package com.jk.socialsync.services;

import com.jk.socialsync.dtos.requests.LoginRequestDto;
import com.jk.socialsync.dtos.requests.SignupRequestDto;
import com.jk.socialsync.dtos.responses.LoginResponseDto;
import com.jk.socialsync.dtos.responses.SignupResponseDto;
import com.jk.socialsync.entities.TokenEntity;
import com.jk.socialsync.entities.UserEntity;
import com.jk.socialsync.repositories.TokenRepository;
import com.jk.socialsync.repositories.UserRepository;
import com.jk.socialsync.security.JwtUtil;
import com.jk.socialsync.types.TokenType;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
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

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        invalidatePreviousTokens(user.getUserId());

        saveToken(accessToken,user,TokenType.ACCESS);
        saveToken(refreshToken,user,TokenType.REFRESH);

        return SignupResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
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

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        invalidatePreviousTokens(user.getUserId());

        saveToken(accessToken,user,TokenType.ACCESS);
        saveToken(refreshToken,user,TokenType.REFRESH);

        return LoginResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // get new access token with the help of refresh token
    public LoginResponseDto refresh(HttpServletRequest request, HttpServletResponse response) {

        // first extract the refresh token
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Unauthorized");
        }

        String refreshToken = authHeader.substring(7);

        String username = jwtUtil.getUsernameFromToken(refreshToken);

        Optional<UserEntity> optionalUser = userRepository.findByUsername(username);

        if(optionalUser.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }

        UserEntity user = optionalUser.get();

        // now check if the refresh token is valid
        Boolean isValid = jwtUtil.isValid(refreshToken,TokenType.REFRESH, user);

        if(!isValid) {
            throw new JwtException("Refresh Token is not valid");
        }

        // invalidate the refresh token
        String refreshTokenHash = jwtUtil.hash(refreshToken);
        TokenEntity oldRefreshToken = tokenRepository.findByTokenHash(refreshTokenHash)
                .orElseThrow(() -> new JwtException("Refresh toke not found"));

        oldRefreshToken.setIsLoggedOut(true);
        oldRefreshToken.setRevokedAt(Instant.now());
        tokenRepository.save(oldRefreshToken);

        // finally, return the new access token and refresh token
        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        saveToken(newAccessToken, user, TokenType.ACCESS);
        saveToken(newRefreshToken, user, TokenType.REFRESH);

        return LoginResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    private void invalidatePreviousTokens(Long userId) {
        List<TokenEntity> validTokensByUser = tokenRepository.findAllTokensByUser(userId);

        if(!validTokensByUser.isEmpty()) {
            validTokensByUser.forEach(t -> {
                t.setIsLoggedOut(true);
            });
        }

        tokenRepository.saveAll(validTokensByUser);
    }

    public void saveToken(String token, UserEntity user, TokenType tokenType) {

        String tokenHash = jwtUtil.hash(token);

        long validity;
        if(tokenType == TokenType.ACCESS) validity = JwtUtil.ACCESS_VALIDITY;
        else validity = JwtUtil.REFRESH_VALIDITY;

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusMillis(validity);

        TokenEntity tokenEntity = TokenEntity.builder()
                .tokenHash(tokenHash)
                .isLoggedOut(false)
                .user(user)
                .tokenType(tokenType)
                .issuedAt(Instant.now())
                .expiresAt(expiresAt)
                .build();

        tokenRepository.save(tokenEntity);
    }
}
