package co.com.crediya.model.user.value;

import co.com.crediya.model.user.exceptions.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "   "})
    void nullOrBlank_throwsDomainValidation(String input) {
        var ex = assertThrows(DomainValidationException.class, () -> new Email(input));
         assertTrue(ex.getMessage().contains("INVALID_EMAIL:empty"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "a@", "@a.com", "a@b", "a@b.", "a b@c.com", "no-at.com", "a@b.c"
    })
    void badFormat_throwsDomainValidation(String input) {
        var ex = assertThrows(DomainValidationException.class, () -> new Email(input));
         assertTrue(ex.getMessage().contains("INVALID_EMAIL:format"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "john_doe+tag@sub.domain.org",
            "a-b.c@x-y.zw"
    })
    void validEmails_constructsAndKeepsValue(String input) {
        var email = new Email(input);
        assertEquals(input, email.value());
    }

    @Test
    void toString_hashCode_equals_noLanzan() {
        var e1 = new Email("user@example.com");
        var e2 = new Email("user@example.com");
        var e3 = new Email("other@example.com");

        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertTrue(e1.toString().contains("user@example.com"));
    }
}
