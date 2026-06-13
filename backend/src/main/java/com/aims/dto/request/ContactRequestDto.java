package com.aims.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactRequestDto {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    private String subject;

    @NotBlank
    private String message;
}
