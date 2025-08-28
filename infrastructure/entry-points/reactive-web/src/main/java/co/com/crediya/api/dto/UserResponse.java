package co.com.crediya.api.dto;

import java.math.BigDecimal;

public record UserResponse (
        String id,
        String name,
        String lastName,
        String email,
        String birthDate,
        String phone,
        String address,
        BigDecimal salary) {}
