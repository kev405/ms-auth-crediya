package co.com.crediya.r2dbc.user;

import co.com.crediya.r2dbc.user.entity.UserEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserReactiveRepository extends ReactiveCrudRepository<UserEntity, String>, ReactiveQueryByExampleExecutor<UserEntity> {

    Mono<Boolean> existsByEmail(String email);

    @Query("""
           SELECT r.name
           FROM role r
           JOIN user_role ur ON ur.role_id = r.id
           WHERE ur.user_id = :userId
           ORDER BY r.name
           """)
    Flux<String> findRoleNamesByUserId(UUID userId);

    @Query("""
           SELECT *
           FROM crediya_users
           WHERE lower(email) = lower(:email)
           """)
    Mono<UserEntity> findRowByEmail(String email);

}
