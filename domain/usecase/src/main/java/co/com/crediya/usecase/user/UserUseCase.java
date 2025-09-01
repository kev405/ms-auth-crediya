package co.com.crediya.usecase.user;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exceptions.DomainConflictException;
import co.com.crediya.model.user.gateways.TxRunner;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Log
public class UserUseCase implements UserUseCaseInterface {

    private final UserRepository userRepository;

    private final TxRunner txRunner;

    @Override
    public Mono<User> save(User user) {
        log.info("Creating user. email= " + user.email().value() +
                ", name= " + user.name() +
                ", lastName= " + user.lastName());

        return txRunner.required(() -> userRepository.existsByEmail(user.email().value())
                .flatMap(exists -> exists
                        ? Mono.error(new DomainConflictException("EMAIL_TAKEN", user.email().value()))
                        : userRepository.save(user))
                .doOnSuccess(u -> log.info("User Created id= " + u.id())));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        log.info("Deleting user. id= " + id);

        return txRunner.required(() -> userRepository.deleteById(id)
                .doOnSuccess(v -> log.info("User Deleted id= " + id)));
    }

    @Override
    public Flux<User> getAllUsers() {
        log.info("Getting all users");
        return txRunner.readOnlyMany(userRepository::getAllUsers);
    }

    @Override
    public Mono<Boolean> existUserByEmail(String email) {
        log.info("Checking if user exists by email= " + email);
        return txRunner.readOnly(() -> userRepository.existsByEmail(email)
                .doOnSuccess(u -> log.info("User exists by email= " + email)));
    }


}
