package co.com.crediya.r2dbc.role.entity;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;
import org.springframework.data.relational.core.mapping.Table;

@Table("role")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RoleEntity {

    @Id
    private UUID id;

    @NotNull
    private String name;
}
