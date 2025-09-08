package co.com.crediya.r2dbc.user.mapper;

import co.com.crediya.model.user.value.Email;
import co.com.crediya.model.user.value.PhoneNumber;
import co.com.crediya.model.user.value.Salary;
import org.mapstruct.ObjectFactory;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class UserValueConverters {

    // --- Factories (Entidad->Dominio) ---
    @ObjectFactory public Email toEmail(String value) { return new Email(value); }
    @ObjectFactory public PhoneNumber toPhone(String value) { return new PhoneNumber(value); }
    @ObjectFactory public Salary toSalary(BigDecimal value) { return new Salary(value); }

    // --- Extractores (Dominio->Entidad) ---
    @Named("emailToString")  public String emailToString(Email e) { return e == null ? null : e.value(); }
    @Named("phoneToString")  public String phoneToString(PhoneNumber p) { return p == null ? null : p.value(); }
    @Named("salaryToBigDecimal") public BigDecimal salaryToBigDecimal(Salary s) { return s == null ? null : s.value(); }

    // --- ID String <-> UUID (ajusta si tu dominio cambia a UUID) ---
    @Named("uuidToString") public String uuidToString(UUID id) { return id == null ? null : id.toString(); }
    @Named("stringToUuid") public UUID stringToUuid(String id) { return id == null ? null : UUID.fromString(id); }
}
