package co.com.crediya.r2dbc.entity;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("crediya_users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserEntity {

    @Id
    private UUID id;

    @NotNull
    private String name;

    @NotNull
    private String lastName;

    @NotNull
    @Column(unique = true)
    private String email;

    @NotNull
    private String address;

    @NotNull
    private LocalDate birthDate;

    @NotNull
    private String phone;

    @NotNull
    private BigDecimal salary;

}
