package co.com.crediya.model.user.value;

import co.com.crediya.model.user.exceptions.DomainValidationException;

public record Email(String value) {
    private static final String REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public Email {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("INVALID_EMAIL", "empty");
        }
        if (!value.matches(REGEX)) {
            throw new DomainValidationException("INVALID_EMAIL", "format");
        }
    }
}
