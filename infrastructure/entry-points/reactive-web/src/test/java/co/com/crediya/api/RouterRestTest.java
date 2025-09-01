package co.com.crediya.api;

import co.com.crediya.api.dto.UserPath;
import co.com.crediya.api.dto.UserResponse;
import co.com.crediya.api.mapper.UserDTOMapper;
import co.com.crediya.api.validation.DtoValidator;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.value.Email;
import co.com.crediya.model.user.value.PhoneNumber;
import co.com.crediya.model.user.value.Salary;
import co.com.crediya.usecase.user.UserUseCaseInterface;
import reactor.core.publisher.Flux;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@EnableConfigurationProperties(UserPath.class)
@WebFluxTest
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserPath userPath;

    @MockitoBean
    private UserUseCaseInterface useCase;

    @MockitoBean
    private UserDTOMapper userDTOMapper;

    @MockitoBean
    private DtoValidator dtoValidator;

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

    @Test
    void testListenGETUseCase() {

        when(useCase.getAllUsers()).thenReturn(Flux.just(user1, user2));

        when(userDTOMapper.toUserResponse(user1)).thenReturn(response1);
        when(userDTOMapper.toUserResponse(user2)).thenReturn(response2);

        webTestClient.get()
                .uri(userPath.getUsers())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponse.class)
                .hasSize(2)
                .value(userResponse -> {
                            Assertions.assertThat(userResponse).isNotEmpty();
                            Assertions.assertThat(userResponse.get(0)).isEqualTo(response1);
                            Assertions.assertThat(userResponse.get(1)).isEqualTo(response2);
                        }
                );

        verify(useCase).getAllUsers();
        verify(userDTOMapper).toUserResponse(user1);
        verify(userDTOMapper).toUserResponse(user2);
        verifyNoMoreInteractions(useCase, userDTOMapper, dtoValidator);
    }
}
