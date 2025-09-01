package co.com.crediya.model.user.value;

import co.com.crediya.model.user.exceptions.DomainValidationException;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class SalaryTest {

    @ParameterizedTest
    @NullSource
    void null_throwsDomainValidation(BigDecimal value) {
        var ex = assertThrows(DomainValidationException.class, () -> new Salary(value));
        assertTrue(ex.getMessage().contains("INVALID_SALARY:null"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "-1000", "-0.01"})
    void negative_throwsDomainValidation(BigDecimal value) {
        var ex = assertThrows(DomainValidationException.class, () -> new Salary(value));
        assertTrue(ex.getMessage().contains("INVALID_SALARY:negative"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"15000001", "20000000", "100000000" })
    void moreThanMax_throwsDomainValidation(BigDecimal value) {
        var ex = assertThrows(DomainValidationException.class, () -> new Salary(value));
        assertTrue(ex.getMessage().contains("INVALID_SALARY:more than 15000000"));
    }

    @Test
    void validSalary_constructsAndKeepsValue() {
        var validSalaries = new BigDecimal[] {
            new BigDecimal("0"),
            new BigDecimal("5000000"),
            new BigDecimal("15000000"),
            new BigDecimal("1234567.89")
        };

        for (var salaryValue : validSalaries) {
            var salary = new Salary(salaryValue);
            assertEquals(salary.value(), salaryValue);
        }
    }
}
