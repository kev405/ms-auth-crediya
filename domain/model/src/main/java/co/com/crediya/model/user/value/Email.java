package co.com.crediya.model.user.value;

import co.com.crediya.model.user.exceptions.DomainValidationException;

public record Email(String value) {
    private static final String REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    public Email {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("INVALID_EMAIL", "empty");
        }
        if (!value.matches(REGEX)) {
            // Si tu regla de negocio exige .com, cambia REGEX a "^[^@\\s]+@[^@\\s]+\\.com$"
            throw new DomainValidationException("INVALID_EMAIL", "format");
        }
    }
}
