package co.com.crediya.model.user.value;

import co.com.crediya.model.user.exceptions.DomainValidationException;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PhoneNumberTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "   "})
    void nullOrBlank_throwsDomainValidation(String input) {
        var ex = assertThrows(DomainValidationException.class, () -> new PhoneNumber(input));
        assertTrue(ex.getMessage().contains("INVALID_PHONE:empty"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123456", "+1 (234) 567-8900", "001234567890", "+12-3456-7890", "++1234567890", "+123 456 7890"
    })
    void badFormat_throwsDomainValidation(String input) {
        var ex = assertThrows(DomainValidationException.class, () -> new PhoneNumber(input));
        assertTrue(ex.getMessage().contains("INVALID_PHONE:format"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+1234567890",
            "+19876543210",
            "+447911123456",
            "+8613800138000"
    })
    void validPhoneNumbers_constructsAndKeepsValue(String input) {
        var phoneNumber = new PhoneNumber(input);
        assertEquals(phoneNumber.value(), input);
    }

    @Test
    void toString_hashCode_equals_noLanzan() {
        var p1 = new PhoneNumber("+1234567890");
        var p2 = new PhoneNumber("+1234567890");
        var p3 = new PhoneNumber("+19876543210");

        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertTrue(p1.toString().contains("+1234567890"));
    }
}
