package co.com.crediya.model.user;

import co.com.crediya.model.role.Role;
import co.com.crediya.model.user.exceptions.DomainValidationException;
import co.com.crediya.model.user.value.Email;
import co.com.crediya.model.user.value.PhoneNumber;
import co.com.crediya.model.user.value.Salary;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

public class UserTest {

    @ParameterizedTest
    @NullAndEmptySource
    void emptyValue(String input) {
        var ex = assertThrows(DomainValidationException.class, () -> new User("id1", input, "Smith", new Email("alic@a.com"),
                "password123",
                Set.of(new Role("ADMIN")),
                "Address 1",
                LocalDate.parse("1990-01-01"), new PhoneNumber("+1234567890"),
                new Salary(java.math.BigDecimal.valueOf(3000000))));

        assertTrue(ex.getMessage().contains("INVALID_NAME:empty"));
    }

    @Test
    void nullValue() {
        var ex = assertThrows(DomainValidationException.class,
                () -> new User("id2", "Bob", "Johnson", null,
                        "password123",
                        Set.of(new Role("ADMIN")),
                        "Address 2",
                        LocalDate.parse("1985-05-15"),
                        new PhoneNumber("+1987654321"),
                        new Salary(java.math.BigDecimal.valueOf(4000000))));

        assertTrue(ex.getMessage().contains("INVALID_EMAIL:null"));
    }
}
