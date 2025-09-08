package co.com.crediya.model.usercredentials;

import co.com.crediya.model.role.Role;
import co.com.crediya.model.user.User;
import java.util.List;

public record UserCredentials(
        User user,
        String passwordHash,
        List<Role> roles,
        boolean enabled
) {}

