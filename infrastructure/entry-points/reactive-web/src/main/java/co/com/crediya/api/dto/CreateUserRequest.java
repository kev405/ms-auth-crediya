package co.com.crediya.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.Range;

@Schema(name = "CreateUserRequest")
public record CreateUserRequest(
        @Schema(example = "John") @NotBlank String name,
        @Schema(example = "Doe") @NotBlank String lastName,
        @Schema(example = "john.doe@acme.com") @NotBlank @Email String email,
        @Schema(example = "1990-01-15") @NotNull @Past LocalDate birthDate,
        @Schema(example = "+573001112233") @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{1,14}$") String phone,
        @Schema(example = "clave123") @NotBlank String password,
        @Schema(example = "Calle 1 # 2-3") @NotBlank String address,
        @Schema(example = "4500000.00") @NotNull @PositiveOrZero @Range(min = 0, max = 15000000) BigDecimal salary,
        @Schema(example = "[\"CUSTOMER\"]", allowableValues = { "ADMIN", "ADVISOR", "CUSTOMER" }, nullable = true)
        List<@Pattern(regexp = "ADMIN|ADVISOR|CUSTOMER", message = "Rol inválido") String> roles
) {}
