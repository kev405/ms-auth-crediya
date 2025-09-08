package co.com.crediya.model.user.gateways;

import co.com.crediya.model.role.Role;
import co.com.crediya.model.user.value.Email;
import java.time.Duration;
import java.util.Collection;
import java.util.UUID;

public interface TokenProvider {
    String createToken(UUID userId, Email email, String name, Collection<Role> roles, Duration ttl);
}
