package com.jk.socialsync.security;

import com.jk.socialsync.entities.TokenEntity;
import com.jk.socialsync.entities.UserEntity;
import com.jk.socialsync.repositories.TokenRepository;
import com.jk.socialsync.repositories.UserRepository;
import com.jk.socialsync.types.TokenType;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            log.info("incoming req: {}", request.getRequestURI());

            String path = request.getServletPath();
            if (path.startsWith("/auth/")) {
                filterChain.doFilter(request, response);
                return;
            }

            String requestTokenHeader = request.getHeader("Authorization");

            if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = requestTokenHeader.substring(7); // removes "Bearer "
            String username = jwtUtil.getUsernameFromToken(token);

            log.error("DOT COUNT: {}", token.chars().filter(c -> c == '.').count());

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserEntity user = userRepository.findByUsername(username).orElseThrow();

                String tokenHash = jwtUtil.hash(token);

                TokenEntity tokenEntity = tokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() -> new JwtException("Token not found"));

                if (tokenEntity.getIsLoggedOut() || tokenEntity.getExpiresAt().isBefore(Instant.now())) {
                    throw new JwtException("Token revoked or expired");
                }

                if (jwtUtil.isValid(token, TokenType.ACCESS, user)) {
                    UsernamePasswordAuthenticationToken token1
                            = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(token1);
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }
}