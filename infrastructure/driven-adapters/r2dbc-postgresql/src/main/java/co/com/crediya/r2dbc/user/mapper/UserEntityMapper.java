package co.com.crediya.r2dbc.user.mapper;

import co.com.crediya.model.user.User;
import co.com.crediya.r2dbc.user.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserValueConverters.class)
public interface UserEntityMapper {

    @Mapping(target = "email", expression = "java(new Email(entity.getEmail()))")
    @Mapping(target = "phone", expression = "java(new PhoneNumber(entity.getPhone()))")
    @Mapping(target = "salary", expression = "java(new Salary(entity.getSalary()))")
    User toDomain(UserEntity entity);


    @Mapping(target = "email", expression = "java(user.email().value())")
    @Mapping(target = "phone", expression = "java(user.phone().value())")
    @Mapping(target = "salary", expression = "java(user.salary().value())")
    UserEntity toEntity(User user);
}
