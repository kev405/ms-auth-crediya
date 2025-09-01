package co.com.crediya.usecase.user;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exceptions.DomainConflictException;
import co.com.crediya.model.user.gateways.TxRunner;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.model.user.value.Email;
import co.com.crediya.model.user.value.PhoneNumber;
import co.com.crediya.model.user.value.Salary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    private final UserRepository repo = mock(UserRepository.class);
    private final TxRunner runner = mock(TxRunner.class);

    private UserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UserUseCase(repo, runner);
        when(runner.required(any()))
                .thenAnswer(inv -> ((Supplier<Mono<?>>) inv.getArgument(0)).get());

        try {
            when(runner.readOnly(any()))
                    .thenAnswer(inv -> ((Supplier<? extends Publisher<?>>) inv.getArgument(0)).get());
        } catch (Throwable ignore) {
            when(runner.readOnly(any()))
                    .thenAnswer(inv -> {
                        Publisher<?> pub = ((Supplier<? extends Publisher<?>>) inv.getArgument(0)).get();
                        return Mono.from(pub);
                    });
        }
    }

    @Test
    void save_whenEmailNotTaken_savesAndReturnsUser() {
        var newUser = new User(
                null, "Alice", "Smith",
                new Email("alice@example.com"),
                "Address 1",
                LocalDate.parse("1990-01-01"),
                new PhoneNumber("+573001234567"),
                new Salary(new BigDecimal("3000000"))
        );
        var savedUser = new User(
                "uuid-1", "Alice", "Smith",
                new Email("alice@example.com"),
                "Address 1",
                LocalDate.parse("1990-01-01"),
                new PhoneNumber("+573001234567"),
                new Salary(new BigDecimal("3000000"))
        );

        when(repo.existsByEmail("alice@example.com")).thenReturn(Mono.just(false));
        when(repo.save(newUser)).thenReturn(Mono.just(savedUser));

        StepVerifier.create(useCase.save(newUser))
                .expectNext(savedUser)
                .verifyComplete();

        verify(repo).existsByEmail("alice@example.com");
        verify(repo).save(newUser);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void save_whenEmailTaken_emitsConflictAndDoesNotSave() {
        var user = new User(
                null, "Bob", "Johnson",
                new Email("bob@example.com"),
                "Addr",
                LocalDate.parse("1985-05-15"),
                new PhoneNumber("+571234567890"),
                new Salary(new BigDecimal("4000000"))
        );

        when(repo.existsByEmail("bob@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.save(user))
                .expectErrorSatisfies(err -> {
                    assertInstanceOf(DomainConflictException.class, err);
                    assertTrue(err.getMessage() != null && err.getMessage().contains("bob@example.com"));
                })
                .verify();

        verify(repo).existsByEmail("bob@example.com");
        verify(repo, never()).save(any());
        verifyNoMoreInteractions(repo);
    }

    @Test
    void deleteById_forwardsToRepoAndCompletes() {
        when(repo.deleteById("id-1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.deleteById("id-1"))
                .verifyComplete();

        verify(repo).deleteById("id-1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void getAllUsers_forwardsFlux() {

        TxRunner fakeRunner = new TxRunner() {
            @Override
            public <T> Mono<T> required(java.util.function.Supplier<Mono<T>> work) {
                return work.get();
            }
            @Override
            public <T> Flux<T> requiredMany(java.util.function.Supplier<Flux<T>> work) {
                return work.get();
            }

        };

        var useCase = new UserUseCase(repo, fakeRunner);

        var u1 = mock(User.class);
        var u2 = mock(User.class);
        when(repo.getAllUsers()).thenReturn(Flux.just(u1, u2));

        StepVerifier.create(useCase.getAllUsers())
                .expectNext(u1, u2)
                .verifyComplete();

        verify(repo).getAllUsers();
        verifyNoMoreInteractions(repo);
    }

    @Test
    void existUserByEmail_forwardsBoolean() {
        when(repo.existsByEmail("a@b.com")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.existUserByEmail("a@b.com"))
                .expectNext(true)
                .verifyComplete();

        verify(repo).existsByEmail("a@b.com");
        verifyNoMoreInteractions(repo);
    }
}
