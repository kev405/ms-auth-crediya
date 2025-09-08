package co.com.crediya.model.user;
import co.com.crediya.model.role.Role;
import co.com.crediya.model.user.exceptions.DomainValidationException;
import co.com.crediya.model.user.value.Email;
import co.com.crediya.model.user.value.PhoneNumber;
import co.com.crediya.model.user.value.Salary;
import java.time.LocalDate;
import java.util.Set;

public record User(
        String id,
        String name,
        String lastName,
        Email email,
        String passwordHash,
        Set<Role> roles,
        String address,
        LocalDate birthDate,
        PhoneNumber phone,
        Salary salary
) {
    public User {
        requireNonBlank(name, "name");
        requireNonBlank(lastName, "lastName");
        requireNonNull(email, "email");
        requireNonBlank(address, "address");
        requireNonNull(birthDate, "birthDate");
        requireNonNull(phone, "phone");
        requireNonNull(salary, "salary");
    }

    private static void requireNonBlank(String v, String field) {
        if (v == null || v.isBlank()) throw new DomainValidationException(
                "INVALID_" + field.toUpperCase(), "empty");
    }

    private static void requireNonNull(Object v, String field) {
        if (v == null) throw new DomainValidationException(
                "INVALID_" + field.toUpperCase(), "null");
    }
}
