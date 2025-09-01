package co.com.crediya.r2dbc;

import co.com.crediya.model.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import co.com.crediya.r2dbc.entity.UserEntity;
import co.com.crediya.r2dbc.mapper.UserEntityMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserReactiveRepositoryAdapterTest {

    @Mock
    UserReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    @Mock
    UserEntityMapper entityMapper;

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
}
