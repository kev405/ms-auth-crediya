package co.com.crediya.usecase.user;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exceptions.DomainConflictException;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserUseCase implements UserUseCaseInterface {

    private final UserRepository userRepository;

    @Override
    public Mono<User> save(User user) {
        return userRepository.existsByEmail(user.email().value())
                .flatMap(exists -> exists
                        ? Mono.error(new DomainConflictException("EMAIL_TAKEN", user.email().value()))
                        : userRepository.save(user));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return userRepository.deleteById(id);
    }

    @Override
    public Flux<User> getAllUsers() {
        return userRepository.getAllUsers();
    }
}
