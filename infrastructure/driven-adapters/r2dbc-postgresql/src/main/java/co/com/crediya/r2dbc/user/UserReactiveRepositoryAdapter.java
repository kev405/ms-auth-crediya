package co.com.crediya.r2dbc.user;

import co.com.crediya.model.role.Role;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.model.usercredentials.UserCredentials;
import co.com.crediya.r2dbc.user.entity.UserEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import co.com.crediya.r2dbc.user.mapper.UserEntityMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

@Repository
public class UserReactiveRepositoryAdapter extends ReactiveAdapterOperations<
    User,
    UserEntity,
    String,
    UserReactiveRepository
> implements UserRepository {

    private final UserEntityMapper entityMapper;

    private final DatabaseClient db;

    public UserReactiveRepositoryAdapter(UserReactiveRepository repository, ObjectMapper mapper, UserEntityMapper entityMapper,
                                         DatabaseClient db) {
        super(repository, mapper, entityMapper::toDomain);
        this.entityMapper = entityMapper;
        this.db = db;
    }

    @Override
    protected UserEntity toData(User user) {
        return entityMapper.toEntity(user);
    }

    @Override
    public Mono<User> save(User user) {
        return super.save(user);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Flux<User> getAllUsers() {
        return super.findAll();
    }

    @Override
    public Mono<UserCredentials> findCredentialsByEmail(String email) {
        return repository.findRowByEmail(email)
                .flatMap(userEntity ->
                        findRolesByUserId(userEntity.getId())
                                .defaultIfEmpty(List.of())
                                .map(roles -> new UserCredentials(
                                        entityMapper.toDomain(userEntity),
                                        userEntity.getPasswordHash(),
                                        roles,
                                        true
                                ))
                );
    }

    @Override
    public Mono<Void> assignRoles(UUID userId, Collection<Role> roles) {
        if (roles == null || roles.isEmpty()) return Mono.empty();

        final String SQL = """
            INSERT INTO user_role (user_id, role_id)
            SELECT :userId, r.id
            FROM role r
            WHERE r.name = :name
            ON CONFLICT DO NOTHING
            """;

        return Flux.fromIterable(roles)
                .flatMap(role ->
                        db.sql(SQL)
                                .bind("userId", userId)
                                .bind("name", role.name())
                                .fetch()
                                .rowsUpdated()
                )
                .then();
    }

    @Override
    public Mono<List<Role>> findRolesByUserId(UUID userId) {
        return repository.findRoleNamesByUserId(userId)
                .map(Role::new)
                .collectList();
    }

    @Override
    public Mono<Void> replaceRoles(UUID userId, Collection<Role> roles) {
        final String SQL_DELETE = "DELETE FROM user_role WHERE user_id = :userId";
        return db.sql(SQL_DELETE)
                .bind("userId", userId)
                .fetch()
                .rowsUpdated()
                .then(assignRoles(userId, roles));
    }
}
