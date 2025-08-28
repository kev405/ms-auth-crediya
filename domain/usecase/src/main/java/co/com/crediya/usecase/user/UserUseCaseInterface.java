package co.com.crediya.usecase.user;

import co.com.crediya.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserUseCaseInterface {
    Mono<User> save(User user);

    Mono<Void> deleteById(String id);

    Flux<User> getAllUsers();
}
