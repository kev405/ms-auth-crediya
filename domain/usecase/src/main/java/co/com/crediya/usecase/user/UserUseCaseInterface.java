package co.com.crediya.usecase.user;

import co.com.crediya.model.role.Role;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.UserLogin;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;

public interface UserUseCaseInterface {
    Mono<User> save(User user);

    Mono<Void> deleteById(String id);

    Flux<User> getAllUsers();

    Mono<Boolean> existUserByEmail(String email);

    public Mono<String> login(UserLogin userWithCredentials);
}
