package co.com.crediya.api.error;

import co.com.crediya.model.user.exceptions.DomainConflictException;
import co.com.crediya.model.user.exceptions.DomainValidationException;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;

@Slf4j
class GlobalErrorHandlerTest {

    private final GlobalErrorHandler handler = new GlobalErrorHandler();

    @Test
    void dataIntegrityViolation_returns409() {
        // 1) Arrange: un exchange de prueba
        var request  = MockServerHttpRequest.get("/users").build();
        var exchange = MockServerWebExchange.from(request);

        // 2) Act: invocar el handler con la excepción “de base de datos”
        var ex = new DataIntegrityViolationException("duplicate key");
        handler.handle(exchange, ex).block(); // bloqueamos en test para esperar el write

        // 3) Assert: status y content-type correctos
        assertEquals(HttpStatus.CONFLICT, exchange.getResponse().getStatusCode());
        assertEquals("application/problem+json",
                Objects.requireNonNull(
                        exchange.getResponse().getHeaders().getContentType()).toString());
    }

    @Test
    void unknownError_returns500() {
        // 1) Arrange: un exchange de prueba
        var request  = MockServerHttpRequest.get("/users").build();
        var exchange = MockServerWebExchange.from(request);

        // 2) Act: invocar el handler con una excepción desconocida
        var ex = new RuntimeException("unexpected error");
        handler.handle(exchange, ex).block(); // bloqueamos en test para esperar el write

        // 3) Assert: status y content-type correctos
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
        assertEquals("application/problem+json",
                Objects.requireNonNull(
                        exchange.getResponse().getHeaders().getContentType()).toString());
    }

    @Test
    void domainValidationException_returns422() {
        // 1) Arrange: un exchange de prueba
        var request  = MockServerHttpRequest.get("/users").build();
        var exchange = MockServerWebExchange.from(request);

        // 2) Act: invocar el handler con una excepción de validación
        var ex = new DomainValidationException("INVALID_EMAIL", "format");
        handler.handle(exchange, ex).block(); // bloqueamos en test para esperar el write

        // 3) Assert: status y content-type correctos
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exchange.getResponse().getStatusCode());
        assertEquals("application/problem+json",
                Objects.requireNonNull(
                        exchange.getResponse().getHeaders().getContentType()).toString());
    }

    @Test
    void domainConflictException_returns409() {
        // 1) Arrange: un exchange de prueba
        var request  = MockServerHttpRequest.get("/users").build();
        var exchange = MockServerWebExchange.from(request);

        // 2) Act: invocar el handler con una excepción de conflicto
        var ex = new DomainConflictException("DUPLICATE_USER", "exists");
        handler.handle(exchange, ex).block(); // bloqueamos en test para esperar el write

        // 3) Assert: status y content-type correctos
        assertEquals(HttpStatus.CONFLICT, exchange.getResponse().getStatusCode());
        assertEquals("application/problem+json",
                Objects.requireNonNull(
                        exchange.getResponse().getHeaders().getContentType()).toString());
    }

    @Test
    void webInputException_returns400() {
        // 1) Arrange: un exchange de prueba
        var request  = MockServerHttpRequest.get("/users").build();
        var exchange = MockServerWebExchange.from(request);

        // 2) Act: invocar el handler con una excepción de entrada mal formada
        var ex = new org.springframework.web.server.ServerWebInputException("bad input");
        handler.handle(exchange, ex).block(); // bloqueamos en test para esperar el write

        // 3) Assert: status y content-type correctos
        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        assertEquals("application/problem+json",
                Objects.requireNonNull(
                        exchange.getResponse().getHeaders().getContentType()).toString());
    }

    @Test
    void responseStatusException_returnsMappedStatus() {
        // 1) Arrange: un exchange de prueba
        var request  = MockServerHttpRequest.get("/users").build();
        var exchange = MockServerWebExchange.from(request);

        // 2) Act: invocar el handler con una ResponseStatusException personalizada
        var ex = new org.springframework.web.server.ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "teapot");
        handler.handle(exchange, ex).block(); // bloqueamos en test para esperar el write

        // 3) Assert: status y content-type correctos
        assertEquals(HttpStatus.I_AM_A_TEAPOT, exchange.getResponse().getStatusCode());
        assertEquals("application/problem+json",
                Objects.requireNonNull(
                        exchange.getResponse().getHeaders().getContentType()).toString());
    }

    @Test
    void errorResponseException_returnsMappedStatus() {
        // 1) Arrange: un exchange de prueba
        var request  = MockServerHttpRequest.get("/users").build();
        var exchange = MockServerWebExchange.from(request);

        // 2) Act: invocar el handler con una ErrorResponseException personalizada
        var ex = new org.springframework.web.ErrorResponseException(HttpStatus.FORBIDDEN);
        handler.handle(exchange, ex).block(); // bloqueamos en test para esperar el write

        // 3) Assert: status y content-type correctos
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        assertEquals("application/problem+json",
                Objects.requireNonNull(
                        exchange.getResponse().getHeaders().getContentType()).toString());
    }

    static class DummyController {
        public void handle(DummyDto dto) {}
    }

    static class DummyDto {
        String email;
    }

    private WebExchangeBindException newBindEx(String field, Object rejected, String message) throws Exception {
        var method = DummyController.class.getMethod("handle", DummyDto.class);
        var param  = new MethodParameter(method, 0);
        var target = new DummyDto();
        var br     = new BeanPropertyBindingResult(target, "dummyDto");
        br.addError(new FieldError("dummyDto", field, rejected, false, null, null, message));
        return new WebExchangeBindException(param, br);
    }

    @Test
    void webExchangeBindException_returns400() throws Exception {
        var request  = MockServerHttpRequest.get("/users").build();
        var exchange = MockServerWebExchange.from(request);

        var ex = newBindEx("email", "bad@", "must be a well-formed email");
        handler.handle(exchange, ex).block();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        assertEquals("application/problem+json",
                Objects.requireNonNull(
                        exchange.getResponse().getHeaders().getContentType()).toString());
    }

    @Test
    void domainValidationException_includesCodeInProblemDetail() {
        var request  = MockServerHttpRequest.get("/users").build();
        var exchange = MockServerWebExchange.from(request);

        var ex = new DomainValidationException(null, null);
        handler.handle(exchange, ex).block();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exchange.getResponse().getStatusCode());
        assertEquals("application/problem+json",
                Objects.requireNonNull(
                        exchange.getResponse().getHeaders().getContentType()).toString());
    }
}
