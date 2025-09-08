package co.com.crediya.r2dbc.user;

import co.com.crediya.model.role.Role;
import co.com.crediya.model.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import co.com.crediya.r2dbc.user.entity.UserEntity;
import co.com.crediya.r2dbc.user.mapper.UserEntityMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class UserReactiveRepositoryAdapterTest {

    @Mock
    UserReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    @Mock
    UserEntityMapper entityMapper;

    @Mock
    DatabaseClient db;

    @InjectMocks
    UserReactiveRepositoryAdapter adapter;

    @Test
    void save_mapsDomainToEntity_andBack() {

        User domainIn   = mock(User.class);
        UserEntity ent  = mock(UserEntity.class);
        User domainOut  = mock(User.class);

        when(entityMapper.toEntity(domainIn)).thenReturn(ent);
        when(repository.save(ent)).thenReturn(Mono.just(ent));
        when(entityMapper.toDomain(ent)).thenReturn(domainOut);

        StepVerifier.create(adapter.save(domainIn))
                .expectNext(domainOut)
                .verifyComplete();

        verify(entityMapper).toEntity(domainIn);
        verify(repository).save(ent);
        verify(entityMapper).toDomain(ent);
        verifyNoMoreInteractions(repository, entityMapper, mapper);
    }

    @Test
    void getAllUsers_mapsEntitiesToDomain() {
        UserEntity e1 = mock(UserEntity.class);
        UserEntity e2 = mock(UserEntity.class);
        User u1 = mock(User.class);
        User u2 = mock(User.class);

        when(repository.findAll()).thenReturn(Flux.just(e1, e2));
        when(entityMapper.toDomain(e1)).thenReturn(u1);
        when(entityMapper.toDomain(e2)).thenReturn(u2);

        StepVerifier.create(adapter.getAllUsers())
                .expectNext(u1, u2)
                .verifyComplete();

        verify(repository).findAll();
        verify(entityMapper).toDomain(e1);
        verify(entityMapper).toDomain(e2);
        verifyNoMoreInteractions(repository, entityMapper, mapper);
    }

    @Test
    void existsByEmail_delegatesToRepository() {
        when(repository.existsByEmail("a@b.com")).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsByEmail("a@b.com"))
                .expectNext(true)
                .verifyComplete();

        verify(repository).existsByEmail("a@b.com");
        verifyNoMoreInteractions(repository, entityMapper, mapper);
    }

    @Test
    void deleteById_delegatesAndCompletes() {
        when(repository.deleteById("id-1")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById("id-1"))
                .verifyComplete();

        verify(repository).deleteById("id-1");
        verifyNoMoreInteractions(repository, entityMapper, mapper);
    }

    @Test
    void findRolesByUserId_mapsNamesToRoleObjects() {
        var userId = UUID.randomUUID();
        when(repository.findRoleNamesByUserId(userId)).thenReturn(Flux.just("ADMIN", "CUSTOMER"));

        StepVerifier.create(adapter.findRolesByUserId(userId))
                .expectNext(List.of(new Role("ADMIN"), new Role("CUSTOMER")))
                .verifyComplete();

        verify(repository).findRoleNamesByUserId(userId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findCredentialsByEmail_whenUserExistsWithRoles_returnsCredentials() {
        // Arrange
        var email = "test@user.com";
        var userId = UUID.randomUUID();
        var userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setPasswordHash("hashedPassword");

        var userDomain = mock(User.class);

        when(repository.findRowByEmail(email)).thenReturn(Mono.just(userEntity));
        when(repository.findRoleNamesByUserId(userId)).thenReturn(Flux.just("ADMIN"));
        when(entityMapper.toDomain(userEntity)).thenReturn(userDomain);

        StepVerifier.create(adapter.findCredentialsByEmail(email))
                .expectNextMatches(credentials ->
                        credentials.user().equals(userDomain) &&
                                credentials.passwordHash().equals("hashedPassword") &&
                                credentials.roles().equals(List.of(new Role("ADMIN"))) &&
                                credentials.enabled()
                )
                .verifyComplete();

        verify(repository).findRowByEmail(email);
        verify(repository).findRoleNamesByUserId(userId);
        verify(entityMapper).toDomain(userEntity);
    }

    @Test
    void findCredentialsByEmail_whenUserExistsWithoutRoles_returnsCredentialsWithEmptyRoles() {
        // Arrange
        var email = "test@user.com";
        var userId = UUID.randomUUID();
        var userEntity = new UserEntity();
        userEntity.setId(userId);

        var userDomain = mock(User.class);

        when(repository.findRowByEmail(email)).thenReturn(Mono.just(userEntity));
        when(repository.findRoleNamesByUserId(userId)).thenReturn(Flux.empty()); // No roles found
        when(entityMapper.toDomain(userEntity)).thenReturn(userDomain);

        // Act & Assert
        StepVerifier.create(adapter.findCredentialsByEmail(email))
                .expectNextMatches(credentials -> credentials.roles().isEmpty()) // Verify roles list is empty
                .verifyComplete();
    }

    @Test
    void assignRoles_whenRolesAreProvided_executesInserts() {
        var userId = UUID.randomUUID();
        var roles = List.of(new Role("ADMIN"), new Role("CUSTOMER"));

        var executeSpecMock = mock(DatabaseClient.GenericExecuteSpec.class);
        var fetchSpecMock = mock(FetchSpec.class);

        when(db.sql(anyString())).thenReturn(executeSpecMock);
        when(executeSpecMock.bind(anyString(), any())).thenReturn(executeSpecMock);
        when(executeSpecMock.fetch()).thenReturn(fetchSpecMock);
        when(fetchSpecMock.rowsUpdated()).thenReturn(Mono.just(1l));

        StepVerifier.create(adapter.assignRoles(userId, roles))
                .verifyComplete();

        verify(db, times(2)).sql(anyString());
    }

    @Test
    void assignRoles_whenRolesAreEmpty_doesNothing() {
        var userId = UUID.randomUUID();

        StepVerifier.create(adapter.assignRoles(userId, List.of()))
                .verifyComplete();

        verifyNoInteractions(db);
    }

    @Test
    void replaceRoles_executesDeleteThenAssignsNewRoles() {
        // Arrange
        var userId = UUID.randomUUID();
        var roles = List.of(new Role("ADMIN"));

        var executeSpecMock = mock(DatabaseClient.GenericExecuteSpec.class);
        var fetchSpecMock = mock(FetchSpec.class);

        // Mock para la operación DELETE y la operación INSERT
        when(db.sql(anyString())).thenReturn(executeSpecMock);
        when(executeSpecMock.bind(anyString(), any())).thenReturn(executeSpecMock);
        when(executeSpecMock.fetch()).thenReturn(fetchSpecMock);
        when(fetchSpecMock.rowsUpdated()).thenReturn(Mono.just(1l));

        StepVerifier.create(adapter.replaceRoles(userId, roles))
                .verifyComplete();

        verify(db, times(2)).sql(anyString());
    }
}
