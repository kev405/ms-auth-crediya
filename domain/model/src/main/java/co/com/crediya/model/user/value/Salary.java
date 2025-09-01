package co.com.crediya.model.user.value;

import co.com.crediya.model.user.exceptions.DomainValidationException;
import java.math.BigDecimal;

public record Salary(BigDecimal value) {
    public Salary {
        if (value == null)
            throw new DomainValidationException("INVALID_SALARY", "null");
        if (value.signum() < 0)
            throw new DomainValidationException("INVALID_SALARY", "negative");
        if (value.intValue() > 15000000)
            throw new DomainValidationException("INVALID_SALARY", "more than 15000000");
    }
}
