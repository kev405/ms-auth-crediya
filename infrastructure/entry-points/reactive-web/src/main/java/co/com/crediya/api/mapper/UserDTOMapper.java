package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.CreateUserRequest;
import co.com.crediya.api.dto.UserResponse;
import co.com.crediya.model.user.User;
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
    User toModel(CreateUserRequest request);

}
