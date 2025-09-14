package co.com.crediya.usecase.user;

import co.com.crediya.model.role.Role;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.UserLogin;
import co.com.crediya.model.user.exceptions.DomainConflictException;
import co.com.crediya.model.user.exceptions.DomainUnauthorizedException;
import co.com.crediya.model.user.gateways.PasswordHasher;
import co.com.crediya.model.user.gateways.TokenProvider;
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
import co.com.crediya.model.usercredentials.UserCredentials;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    private final UserRepository repo = mock(UserRepository.class);
    private final TxRunner runner = mock(TxRunner.class);
    private final PasswordHasher hasher = mock(PasswordHasher.class);
    private final TokenProvider tokenProvider = mock(TokenProvider.class);

    private UserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UserUseCase(repo, hasher, tokenProvider, runner);
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
                "password123",
                Set.of(new Role("ADMIN")),
                "Address 1",
                LocalDate.parse("1990-01-01"),
                new PhoneNumber("+573001234567"),
                new Salary(new BigDecimal("3000000"))
        );
        var savedUser = new User(
                "2b3d8236-fefa-4c2c-aa71-4cf50176249a", "Alice", "Smith",
                new Email("alice@example.com"),
                null,
                Set.of(new Role("ADMIN")),
                "Address 1",
                LocalDate.parse("1990-01-01"),
                new PhoneNumber("+573001234567"),
                new Salary(new BigDecimal("3000000"))
        );

        when(repo.existsByEmail("alice@example.com")).thenReturn(Mono.just(false));
        when(hasher.hash("password123")).thenReturn("password123");
        when(repo.save(newUser)).thenReturn(Mono.just(savedUser));
        when(repo.assignRoles(any(UUID.class), any(Set.class))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.save(newUser))
                .expectNext(savedUser)
                .verifyComplete();

        verify(repo).existsByEmail("alice@example.com");
        verify(repo).save(newUser);
        verify(repo).assignRoles(any(UUID.class), any(Set.class));
        verifyNoMoreInteractions(repo);
    }

    @Test
    void save_whenEmailNotTaken_whitoutRoles_savesAndReturnsUser() {
        var newUser = new User(
                null, "Alice", "Smith",
                new Email("alice@example.com"),
                "password123",
                null,
                "Address 1",
                LocalDate.parse("1990-01-01"),
                new PhoneNumber("+573001234567"),
                new Salary(new BigDecimal("3000000"))
        );
        var savedUser = new User(
                "2b3d8236-fefa-4c2c-aa71-4cf50176249a", "Alice", "Smith",
                new Email("alice@example.com"),
                null,
                null,
                "Address 1",
                LocalDate.parse("1990-01-01"),
                new PhoneNumber("+573001234567"),
                new Salary(new BigDecimal("3000000"))
        );

        when(repo.existsByEmail("alice@example.com")).thenReturn(Mono.just(false));
        when(hasher.hash("password123")).thenReturn("password123");
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
                "password123",
                Set.of(new Role("ADMIN")),
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
    void login_whenCredentialsAreValid_returnsToken() {
        // Arrange
        var userLogin = new UserLogin("test@user.com", "password123");
        var user = new User("2b3d8236-fefa-4c2c-aa71-4cf50176249a", "Test", "User", new Email("test@user.com"),
                "hashedPassword", Set.of(new Role("CUSTOMER")), "Address 1",
                LocalDate.parse("1990-01-01"),
                new PhoneNumber("+573001234567"),
                new Salary(new BigDecimal("3000000")));
        var userCredentials = new UserCredentials(user, "hashedPassword", List.of(new Role("CUSTOMER")), true);
        var expectedToken = "fake-jwt-token";

        when(repo.findCredentialsByEmail("test@user.com")).thenReturn(Mono.just(userCredentials));
        when(hasher.matches("password123", "hashedPassword")).thenReturn(true);
        when(tokenProvider.createToken(any(UUID.class), any(Email.class), anyString(), anyList(), any(Duration.class)))
                .thenReturn(expectedToken);

        // Act & Assert
        StepVerifier.create(useCase.login(userLogin))
                .expectNext(expectedToken)
                .verifyComplete();

        verify(repo).findCredentialsByEmail("test@user.com");
        verify(hasher).matches("password123", "hashedPassword");
        verify(tokenProvider).createToken(any(UUID.class), any(Email.class), anyString(), anyList(), any(
                Duration.class));
        verifyNoMoreInteractions(repo, hasher, tokenProvider);
    }

    @Test
    void login_whenUserNotFound_emitsBadCredentialsError() {
        // Arrange
        var userLogin = new UserLogin("notfound@user.com", "password123");
        when(repo.findCredentialsByEmail("notfound@user.com")).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.login(userLogin))
                .expectErrorSatisfies(err -> {
                    assertInstanceOf(DomainUnauthorizedException.class, err);
                    assertEquals("BAD_CREDENTIALS", err.getMessage());
                })
                .verify();

        verify(repo).findCredentialsByEmail("notfound@user.com");
        verifyNoMoreInteractions(repo, hasher, tokenProvider);
    }

    @Test
    void login_whenPasswordIsIncorrect_emitsBadCredentialsError() {
        // Arrange
        var userLogin = new UserLogin("test@user.com", "wrongPassword");
        var user = new User("user-uuid", "Test", "User", new Email("test@user.com"),
                "hashedPassword", Set.of(new Role("USER")), "Address 1",
                LocalDate.parse("1990-01-01"),
                new PhoneNumber("+573001234567"),
                new Salary(new BigDecimal("3000000")));
        var userCredentials = new UserCredentials(user, "hashedPassword", List.of(new Role("CUSTOMER")), true);

        when(repo.findCredentialsByEmail("test@user.com")).thenReturn(Mono.just(userCredentials));
        when(hasher.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        // Act & Assert
        StepVerifier.create(useCase.login(userLogin))
                .expectErrorSatisfies(err -> {
                    assertInstanceOf(DomainUnauthorizedException.class, err);
                    assertEquals("BAD_CREDENTIALS", err.getMessage());
                })
                .verify();

        verify(repo).findCredentialsByEmail("test@user.com");
        verify(hasher).matches("wrongPassword", "hashedPassword");
        verifyNoMoreInteractions(repo, hasher, tokenProvider);
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

        var useCase = new UserUseCase(repo, hasher, tokenProvider, fakeRunner);

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

    @Test
    void userDataByEmail_forwardsUserData() {
        var ud = mock(co.com.crediya.model.user.UserData.class);
        when(repo.findByEmail("x@y.com")).thenReturn(Mono.just(ud));

        StepVerifier.create(useCase.userDataByEmail("x@y.com"))
                .expectNext(ud)
                .verifyComplete();

        verify(repo).findByEmail("x@y.com");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void save_hashesPassword_andSavesHashedUser_andAssignsRolesWhenPresent() {
        var roles = Set.of(new co.com.crediya.model.role.Role("ADMIN"));
        var newUser = new User(
                null, "Alice", "Smith",
                new Email("alice@example.com"),
                "plain-pass",
                roles,
                "Addr 1",
                LocalDate.parse("1990-01-01"),
                new PhoneNumber("+573001234567"),
                new Salary(new BigDecimal("3000000"))
        );

        var savedId = "2b3d8236-fefa-4c2c-aa71-4cf50176249a";
        var savedUser = new User(
                savedId, "Alice", "Smith",
                new Email("alice@example.com"),
                null,
                roles,
                "Addr 1",
                LocalDate.parse("1990-01-01"),
                new PhoneNumber("+573001234567"),
                new Salary(new BigDecimal("3000000"))
        );

        when(repo.existsByEmail("alice@example.com")).thenReturn(Mono.just(false));
        when(hasher.hash("plain-pass")).thenReturn("HASHED-123");

        var toSaveCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        when(repo.save(toSaveCaptor.capture())).thenReturn(Mono.just(savedUser));
        when(repo.assignRoles(UUID.fromString(savedId), roles)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.save(newUser))
                .expectNext(savedUser)
                .verifyComplete();

        verify(hasher).hash("plain-pass");
        var savedArg = toSaveCaptor.getValue();
        assertEquals("HASHED-123", savedArg.passwordHash());
        assertNull(savedArg.id());

        verify(repo).assignRoles(UUID.fromString(savedId), roles);

        verify(repo).existsByEmail("alice@example.com");
        verify(repo).save(any(User.class));
        verifyNoMoreInteractions(repo, hasher, tokenProvider);
    }

    @Test
    void existUserByEmail_whenRepoErrors_propagatesError() {
        when(repo.existsByEmail("err@x.com")).thenReturn(Mono.error(new RuntimeException("boom")));

        StepVerifier.create(useCase.existUserByEmail("err@x.com"))
                .expectErrorMatches(e -> e.getMessage().contains("boom"))
                .verify();

        verify(repo).existsByEmail("err@x.com");
        verifyNoMoreInteractions(repo);
    }
}
