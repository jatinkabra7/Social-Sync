package com.jk.socialsync.dtos.responses;

import com.jk.socialsync.dtos.responses.common.UserInfoDto;
import com.jk.socialsync.types.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostAddResponseDto {
    private Long id;
    private String mediaUrl;
    private MediaType mediaType;
    private String caption;
    private LocalDateTime uploadedAt;
    private UserInfoDto author;
}
