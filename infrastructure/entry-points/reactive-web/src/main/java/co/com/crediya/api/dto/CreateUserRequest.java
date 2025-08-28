package co.com.crediya.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hibernate.validator.constraints.Range;

public record CreateUserRequest(
        @NotBlank String name,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotNull @Past LocalDate birthDate,
        @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{1,14}$") String phone,
        @NotBlank String address,
        @NotNull @PositiveOrZero @Range(min = 0, max = 15000000) BigDecimal salary
) {}
