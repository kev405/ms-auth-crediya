package co.com.crediya.model.user.gateways;

import co.com.crediya.model.role.Role;
import co.com.crediya.model.user.User;
import co.com.crediya.model.usercredentials.UserCredentials;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserRepository {

    Mono<User> save(User user);

    Mono<Void> deleteById(String id);

    Mono<Boolean> existsByEmail(String email);

    Flux<User> getAllUsers();

    Mono<UserCredentials> findCredentialsByEmail(String email);
    Mono<Void> assignRoles(UUID userId, Collection<Role> roles);
    Mono<List<Role>> findRolesByUserId(UUID userId);
    Mono<Void> replaceRoles(UUID userId, Collection<Role> roles);

}
