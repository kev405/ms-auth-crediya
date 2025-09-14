package co.com.crediya.usecase.user;

import co.com.crediya.model.user.*;
import co.com.crediya.model.user.exceptions.DomainConflictException;
import co.com.crediya.model.user.exceptions.DomainUnauthorizedException;
import co.com.crediya.model.user.gateways.PasswordHasher;
import co.com.crediya.model.user.gateways.TxRunner;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.model.user.gateways.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@Log
public class UserUseCase implements UserUseCaseInterface {

    private final UserRepository userRepository;
    private final PasswordHasher hasher;
    private final TokenProvider tokenProvider;
    private final TxRunner txRunner;

    @Override
    public Mono<User> save(User user) {
        log.info("Creating user. email= " + user.email().value() +
                ", name= " + user.name() +
                ", lastName= " + user.lastName());

        return txRunner.required(() -> userRepository.existsByEmail(user.email().value())
                .flatMap(exists -> exists
                        ? Mono.error(new DomainConflictException("EMAIL_TAKEN", user.email().value()))
                        : Mono.empty())
                .then(Mono.fromSupplier(() -> {
                    String hashed = hasher.hash(user.passwordHash());   // password en claro -> hash
                    // reconstruimos el record con el hash
                    return new User(
                            null,
                            user.name(),
                            user.lastName(),
                            user.email(),
                            hashed,
                            user.roles(),     // pueden venir vacíos; los asignamos abajo si trae valores
                            user.address(),
                            user.birthDate(),
                            user.phone(),
                            user.salary()
                    );
                }))
                .flatMap(userRepository::save)
                .flatMap(saved -> (user.roles() == null || user.roles().isEmpty())
                        ? Mono.just(saved)
                        : userRepository.assignRoles(
                        UUID.fromString(saved.id()), user.roles()).thenReturn(saved))
        ).doOnSuccess(u -> log.info("User Created id= " + u.id()));
    }

    @Override
    public Mono<String> login(UserLogin userWithCredentials) {
        var email = userWithCredentials.email();
        var rawPassword = userWithCredentials.password();
        log.info("Authenticating user. email= " + email);

        return txRunner.required(() ->
                userRepository.findCredentialsByEmail(email)
                        .switchIfEmpty(Mono.error(new DomainUnauthorizedException("BAD_CREDENTIALS")))
                        .flatMap(uc -> {
//                            if (!uc.enabled()) return Mono.error(new DomainUnauthorizedException("USER_DISABLED"));
                            return hasher.matches(rawPassword, uc.passwordHash())
                                    ? Mono.just(uc)
                                    : Mono.error(new DomainUnauthorizedException("BAD_CREDENTIALS"));
                        })
                        .map(uc -> tokenProvider.createToken(
                                UUID.fromString(uc.user().id()), uc.user().email(), uc.user().name(), uc.roles(), Duration.ofHours(2)))
        ).doOnSuccess(t -> log.info("Token issued for email= " + email));
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

    @Override
    public Mono<UserData> userDataByEmail(String email) {
        log.info("Checking if user exists by email= " + email);
        return txRunner.readOnly(() -> userRepository.findByEmail(email)
                .doOnSuccess(u -> log.info("User exists by email= " + email)));
    }
}
