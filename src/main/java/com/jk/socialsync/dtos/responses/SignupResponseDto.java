package com.jk.socialsync.dtos.responses;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SignupResponseDto {
    private Long userId;
    private String username;
    private String email;
    private String jwt;
}
