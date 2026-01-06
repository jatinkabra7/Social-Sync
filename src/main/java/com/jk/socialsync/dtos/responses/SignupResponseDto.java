package com.jk.socialsync.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignupResponseDto {
    private Long userId;
    private String username;
    private String email;
    private String accessToken;
    private String refreshToken;
}