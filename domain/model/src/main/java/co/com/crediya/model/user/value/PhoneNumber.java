package co.com.crediya.model.user.value;

import co.com.crediya.model.user.exceptions.DomainValidationException;

public record PhoneNumber(String value) {
    private static final String REGEX = "^\\+[1-9]\\d{1,14}$";
    public PhoneNumber {
        if (value == null || value.isBlank())
            throw new DomainValidationException("INVALID_PHONE", "empty");
        if (!value.matches(REGEX))
            throw new DomainValidationException("INVALID_PHONE", "format");
    }
}