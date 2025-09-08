package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.CreateUserRequest;
import co.com.crediya.api.dto.LoginRequest;
import co.com.crediya.api.dto.UserResponse;
import co.com.crediya.model.role.Role;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.UserLogin;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserDTOMapper {

    @Mapping(target = "email", expression = "java(user.email().value())")
    @Mapping(target = "phone", expression = "java(user.phone().value())")
    @Mapping(target = "salary", expression = "java(user.salary().value())")
    UserResponse toUserResponse(User user);

    @Mapping(target="email",       expression="java(new Email(request.email()))")
    @Mapping(target="phone", expression="java(new PhoneNumber(request.phone()))")
    @Mapping(target="salary",      expression="java(new Salary(request.salary()))")
    @Mapping(target = "roles",        expression = "java(toRoleSet(request.roles()))")
    @Mapping(target = "passwordHash", expression = "java(request.password())")
    User toModel(CreateUserRequest request);

    UserLogin toModel(LoginRequest loginRequest);

    // ---------- Helpers de colecciones ----------

    /** List<String> (DTO) -> Set<Role> (Model) */
    default Set<Role> toRoleSet(List<String> roles) {
        if (roles == null || roles.isEmpty()) return Set.of();
        return roles.stream()
                .filter(Objects::nonNull)
                .map(Role::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Set<Role> (Model) -> List<String> (DTO/Response) */
    default List<String> toRoleNameList(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) return List.of();
        return roles.stream()
                .filter(Objects::nonNull)
                .map(Role::name)
                .toList();
    }
}
