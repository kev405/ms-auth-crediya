package co.com.crediya.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(
        @Schema(example = "john.doe@acme.com") @NotBlank @Email String email,
        @Schema(example = "clave123") @NotBlank String password) {}
