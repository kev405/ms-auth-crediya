package co.com.crediya.r2dbc;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.r2dbc.entity.UserEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import co.com.crediya.r2dbc.mapper.UserEntityMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserReactiveRepositoryAdapter extends ReactiveAdapterOperations<
    User,
    UserEntity,
    String,
    UserReactiveRepository
> implements UserRepository {

    private final UserEntityMapper entityMapper;

    public UserReactiveRepositoryAdapter(UserReactiveRepository repository, ObjectMapper mapper, UserEntityMapper entityMapper) {
        super(repository, mapper, entityMapper::toDomain);
        this.entityMapper = entityMapper;
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
}
