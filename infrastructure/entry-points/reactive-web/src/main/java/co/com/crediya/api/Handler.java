package co.com.crediya.api;

import co.com.crediya.api.dto.CreateUserRequest;
import co.com.crediya.api.dto.UserResponse;
import co.com.crediya.api.mapper.UserDTOMapper;
import co.com.crediya.api.validation.DtoValidator;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.user.UserUseCaseInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {

    private final UserUseCaseInterface userUseCase;

    private final UserDTOMapper userDTOMapper;

    private final DtoValidator dtoValidator;

    public Mono<ServerResponse> listenSaveUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateUserRequest.class)
                .flatMap(dtoValidator::validate)
                .map(userDTOMapper::toModel)
                .flatMap(userUseCase::save)
                .map(userDTOMapper::toUserResponse)
                .flatMap(savedUserDTO -> ServerResponse.ok()
                        .bodyValue(savedUserDTO));
    }

    public Mono<ServerResponse> listenGetAllUsers(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_NDJSON) // application/x-ndjson
                .body(userUseCase.getAllUsers().map(userDTOMapper::toUserResponse), UserResponse.class);
    }

    public Mono<ServerResponse> listenExistByEmail(ServerRequest serverRequest) {
        String email = serverRequest.pathVariable("email");
        log.info("email: {}", email);
        return userUseCase.existUserByEmail(email).flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> listenDeleteUser(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");

        return userUseCase.deleteById(id)
                .then(ServerResponse.noContent().build());
    }
}
