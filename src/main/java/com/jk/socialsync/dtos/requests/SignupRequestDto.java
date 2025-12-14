package com.jk.socialsync.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {

    @NotBlank(message = "Username is required")
    @Pattern(
            regexp = "^[a-zA-Z0-9_]{3,20}$",
            message = "Username must be 3-20 characters and contain only letters, numbers and underscores"
    )
    private String username;

    @NotBlank(message = "Password is required")
    @Pattern(
            // At least one uppercase letter
            // At least one lowercase letter
            // At least one special symbol
            // length 8-16
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,16}$",
            message = "Password must be 8-16 characters long, contain an uppercase letter, a lowercase letter, and a special character"
    )
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid Email format")
    private String email;

}
