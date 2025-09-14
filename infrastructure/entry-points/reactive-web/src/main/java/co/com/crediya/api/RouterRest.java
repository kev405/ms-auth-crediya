package co.com.crediya.api;

import co.com.crediya.api.dto.CreateUserRequest;
import co.com.crediya.api.dto.UserPath;
import co.com.crediya.api.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final UserPath userPath;

    @Bean
    @RouterOperations({

            // GET /api/users  -> stream NDJSON
            @RouterOperation(
                    path = "/api/users",
                    produces = { MediaType.APPLICATION_NDJSON_VALUE },
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "listenGetAllUsers",
                    operation = @Operation(
                            operationId = "getAllUsers",
                            summary = "List all users (NDJSON stream)",
                            tags = {"Users"},
                            security = { @SecurityRequirement(name = "bearerAuth") },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "OK",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_NDJSON_VALUE,
                                                    array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))
                                            )
                                    ),
                                    @ApiResponse(responseCode = "500", description = "Server error")
                            }
                    )
            ),

            // POST /api/users -> crear usuario (201)
            @RouterOperation(
                    path = "/api/users",
                    consumes = { MediaType.APPLICATION_JSON_VALUE },
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "listenSaveUser",
                    operation = @Operation(
                            operationId = "createUser",
                            summary = "Create user",
                            tags = {"Users"},
                            security = { @SecurityRequirement(name = "bearerAuth") },
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = CreateUserRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Created",
                                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Bad Request",
                                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                                    @ApiResponse(responseCode = "409", description = "Conflict",
                                            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
                            }
                    )
            ),

            // GET /api/users/{email} -> existe por email (true/false)
            @RouterOperation(
                    path = "/api/users/{email}",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "listenExistByEmail",
                    operation = @Operation(
                            operationId = "getExistByEmail",
                            summary = "Check if user exists by email",
                            tags = {"Users"},
                            security = { @SecurityRequirement(name = "bearerAuth") },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "OK",
                                            content = @Content(schema = @Schema(implementation = Boolean.class))),
                                    @ApiResponse(responseCode = "500", description = "Server error")
                            }
                    )
            ),

            @RouterOperation(
                    path = "/api/users/{id}",
                    method = RequestMethod.DELETE,
                    beanClass = Handler.class,
                    beanMethod = "listenDeleteUser",
                    operation = @Operation(
                            operationId = "deleteUser",
                            summary = "Delete user by id",
                            tags = {"Users"},
                            security = { @SecurityRequirement(name = "bearerAuth") },
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "No Content"),
                                    @ApiResponse(responseCode = "404", description = "Not Found",
                                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                                    @ApiResponse(responseCode = "500", description = "Server error")
                            }
                    )
            ),

            @RouterOperation(
                    path = "/api/login",
                    consumes = { MediaType.APPLICATION_JSON_VALUE },
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "listenLogin",
                    operation = @Operation(
                            operationId = "login",
                            summary = "Login with email and password",
                            tags = {"Authentication"},
                            // sin SecurityRequirement: público
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema( // DTO simple: {email,password}
                                            implementation = co.com.crediya.api.dto.LoginRequest.class
                                    ))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "OK",
                                            content = @Content(schema = @Schema(
                                                    implementation = co.com.crediya.api.dto.TokenResponse.class
                                            ))),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                                            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(GET(userPath.getUsers()), handler::listenGetAllUsers)
                .andRoute(POST(userPath.getUsers()), handler::listenSaveUser)
                .andRoute(GET(userPath.getUsersByEmail()), handler::listenExistByEmail)
                .andRoute(GET(userPath.getUsersDataByEmail()), handler::listenUserDataByEmail)
                .andRoute(DELETE(userPath.getUsersById()), handler::listenDeleteUser)
                .andRoute(POST(userPath.getLogin()), handler::listenLogin);
    }
}
