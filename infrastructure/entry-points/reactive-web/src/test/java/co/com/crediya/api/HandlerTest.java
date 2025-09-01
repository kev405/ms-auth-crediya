package co.com.crediya.api;

import co.com.crediya.api.dto.CreateUserRequest;
import co.com.crediya.api.dto.UserPath;
import co.com.crediya.api.dto.UserResponse;
import co.com.crediya.api.mapper.UserDTOMapper;
import co.com.crediya.api.validation.DtoValidator;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.value.Email;
import co.com.crediya.model.user.value.PhoneNumber;
import co.com.crediya.model.user.value.Salary;
import co.com.crediya.usecase.user.UserUseCaseInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class HandlerTest {

    private UserPath userPath;

    private UserUseCaseInterface useCase = mock(UserUseCaseInterface.class);

    private UserDTOMapper mapper = mock(UserDTOMapper.class);

    private DtoValidator validator = mock(DtoValidator.class);

    private WebTestClient client;

    @BeforeEach
    void setUp() {

        userPath = new UserPath();
        userPath.setUsers("/api/users");
        userPath.setUsersById("/api/users/{id}");
        userPath.setUsersByEmail("/api/users/email/{email}");

        var handler = new Handler(useCase, mapper, validator);

        RouterFunction<ServerResponse> router =
                route(POST(userPath.getUsers()), handler::listenSaveUser)
                        .andRoute(GET(userPath.getUsers()), handler::listenGetAllUsers)
                        .andRoute(GET(userPath.getUsersByEmail()), handler::listenExistByEmail)
                        .and(route(DELETE(userPath.getUsersById()), handler::listenDeleteUser));

        client = WebTestClient.bindToRouterFunction(router).build();
    }

    @Test
    void saveUser_happyPath_returns200AndBody() {
        CreateUserRequest dto = new CreateUserRequest("Kevin", "Aristizabal", "karistizabal307@gmail.com",
                LocalDate.parse("2001-01-01"), "+573001234567", "Calle 123 #45-67", java.math.BigDecimal.valueOf(5000000));

        User model = new User(null,"Kevin", "Aristizabal", new Email("karistizabal307@gmail.com"),
                "Calle 123 #45-67", LocalDate.parse("2001-01-01"), new PhoneNumber("+573001234567"),
                new Salary(java.math.BigDecimal.valueOf(5000000)));

        User saved = new User("2e022c3d-66e8-4a78-8d88-87ff8871c944","Kevin", "Aristizabal",
                new Email("karistizabal307@gmail.com"), "Calle 123 #45-67",
                LocalDate.parse("2001-01-01"), new PhoneNumber("+573001234567"),
                new Salary(java.math.BigDecimal.valueOf(5000000)));
        UserResponse responseDto = new UserResponse("2e022c3d-66e8-4a78-8d88-87ff8871c944","Kevin", "Aristizabal",
                "karistizabal307@gmail.com", LocalDate.parse("2001-01-01"), "+573001234567",
                "Calle 123 #45-67", java.math.BigDecimal.valueOf(5000000));

        when(validator.validate(any(CreateUserRequest.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(mapper.toModel(dto)).thenReturn(model);
        when(useCase.save(model)).thenReturn(Mono.just(saved));
        when(mapper.toUserResponse(saved)).thenReturn(responseDto);

        client.post().uri(userPath.getUsers())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                // Si UserResponse es record o tiene equals, puedes usar:
                .expectBody(UserResponse.class).isEqualTo(responseDto);

        verify(validator).validate(any(CreateUserRequest.class));
        verify(mapper).toModel(dto);
        verify(useCase).save(model);
        verify(mapper).toUserResponse(saved);
        verifyNoMoreInteractions(useCase, mapper, validator);
    }

    @Test
    void getAllUsers_happyPath_returns200AndBody() {
        User user1 = new User("id1", "Alice", "Smith", new Email("alic@a.com"),
                "Address 1",
                LocalDate.parse("1990-01-01"), new PhoneNumber("+1234567890"),
                new Salary(java.math.BigDecimal.valueOf(3000000)));
        User user2 = new User("id2", "Bob", "Johnson", new Email("jhona@m.com"),
                "Address 2",
                LocalDate.parse("1985-05-15"), new PhoneNumber("+1987654321"),
                new Salary(java.math.BigDecimal.valueOf(4000000)));
        UserResponse response1 =
                new UserResponse("id1", "Alice", "Smith", "alic@a.com",
                        LocalDate.parse("1990-01-01"), "+1234567890",
                        "Address 1", java.math.BigDecimal.valueOf(3000000));
        UserResponse response2 = new UserResponse("id2", "Bob", "Johnson",
                "jhona@m.com", LocalDate.parse("1985-05-15"), "+1987654321",
                "Address 2", java.math.BigDecimal.valueOf(4000000));

        when(useCase.getAllUsers()).thenReturn(
                Mono.just(user1).concatWith(Mono.just(user2)));
        when(mapper.toUserResponse(user1)).thenReturn(response1);
        when(mapper.toUserResponse(user2)).thenReturn(response2);

        client.get().uri(userPath.getUsers())
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
                .expectBodyList(UserResponse.class)
                .hasSize(2)
                .contains(response1, response2);

        verify(useCase).getAllUsers();
        verify(mapper).toUserResponse(user1);
        verify(mapper).toUserResponse(user2);
        verifyNoMoreInteractions(useCase, mapper, validator);
    }

    @Test
    void existByEmail_happyPath_returns200AndBody() {
        String email = "alic@a.com";
        when(useCase.existUserByEmail(email)).thenReturn(Mono.just(true));
        client.get().uri(userPath.getUsersByEmail(), email)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(Boolean.class).isEqualTo(true);

        verify(useCase).existUserByEmail(email);
        verifyNoMoreInteractions(useCase, mapper, validator);

    }

    @Test
    void deleteUser_happyPath_returns204( ) {
        String userId = "id1";
        when(useCase.deleteById(userId)).thenReturn(Mono.empty());

        client.delete().uri(userPath.getUsersById(), userId)
                .exchange()
                .expectStatus().isNoContent();

        verify(useCase).deleteById(userId);
        verifyNoMoreInteractions(useCase, mapper, validator);
    }
}
