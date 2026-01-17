package com.jk.socialsync.security;

import com.jk.socialsync.entities.TokenEntity;
import com.jk.socialsync.entities.UserEntity;
import com.jk.socialsync.repositories.TokenRepository;
import com.jk.socialsync.types.TokenType;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private static final Long ONE_MINUTE = 1000L * 60;
    private static final Long ONE_DAY = ONE_MINUTE * 60 * 24;
    private static final Long ONE_MONTH = ONE_DAY * 30;

    public static final Long ACCESS_VALIDITY = ONE_MINUTE * 15; // 15 minutes
    public static final Long REFRESH_VALIDITY = ONE_MONTH * 3; // 3 months

    private final TokenRepository tokenRepository;

    @Value("${jwt.Secretkey}")
    private String jwtSecretkey;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretkey.getBytes(StandardCharsets.UTF_8));
    }

    public String hash(String token) {
        return DigestUtils.sha256Hex(token);
    }

    public String generateAccessToken(UserEntity user) {
        return generateToken(user, ACCESS_VALIDITY);
    }

    public String generateRefreshToken(UserEntity user) {
        return generateToken(user, REFRESH_VALIDITY);
    }

    public String generateToken(UserEntity user, Long expirationTime) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSecretKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Boolean isValid(String token, TokenType tokenType, UserEntity user) {
        String username = getUsernameFromToken(token);
        String tokenHash = hash(token);

        TokenEntity tokenEntity = tokenRepository.findByTokenHash(tokenHash).orElse(null);

        if (tokenEntity == null) return false;

        if (tokenEntity.getIsLoggedOut()) return false;

        if (tokenEntity.getTokenType() != tokenType) return false;

        if (tokenEntity.getExpiresAt().isBefore(Instant.now())) return false;

        return username.equals(user.getUsername()) && !isTokenExpired(token);
    }

    public Boolean isTokenExpired(String token) {
        Date expirationTime = getClaims(token).getExpiration();
        return expirationTime.before(new Date());
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
