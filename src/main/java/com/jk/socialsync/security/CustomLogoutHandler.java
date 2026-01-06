package com.jk.socialsync.security;

import com.jk.socialsync.repositories.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final TokenRepository tokenRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        String requestTokenHeader = request.getHeader("Authorization");

        if(requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
            return;
        }

        String token = requestTokenHeader.substring(7); // removes "Bearer "
        String tokenHash = jwtUtil.hash(token);

        // get the stored token from database and mark is as logged out

        tokenRepository.findByTokenHash(tokenHash).ifPresent(t -> {
            t.setIsLoggedOut(true);
            t.setRevokedAt(Instant.now());
            tokenRepository.save(t);
        });
    }
}