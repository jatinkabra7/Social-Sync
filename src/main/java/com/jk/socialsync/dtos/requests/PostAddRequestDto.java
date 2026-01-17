package com.jk.socialsync.dtos.requests;

import com.jk.socialsync.types.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostAddRequestDto {

    @NotBlank(message = "Media URL is required")
    private String mediaUrl;

    @NotNull(message = "Media type is required")
    private MediaType mediaType;

    @Size(max = 500, message = "Caption cannot exceed 500 characters")
    private String caption;

    // user details can be found out by access token in the header of this request
}
