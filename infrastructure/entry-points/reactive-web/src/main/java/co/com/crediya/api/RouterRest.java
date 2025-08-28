package co.com.crediya.api;

import co.com.crediya.api.dto.UserPath;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final UserPath userPath;

    @Bean
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(GET(userPath.getUsers()), handler::listenGetAllUsers)
                .andRoute(POST(userPath.getUsers()), handler::listenSaveUser)
                .and(route(DELETE(userPath.getUsersById()), handler::listenDeleteUser));
    }
}
