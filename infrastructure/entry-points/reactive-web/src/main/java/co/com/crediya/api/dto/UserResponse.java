package co.com.crediya.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserResponse")
public record UserResponse (
        String id,
        String name,
        String lastName,
        String email,
        LocalDate birthDate,
        String phone,
        String address,
        BigDecimal salary) {}
