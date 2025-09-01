package co.com.crediya.api.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import java.util.Collections;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DtoValidatorTest {

    private final Validator validator = mock(Validator.class);
    private DtoValidator sut;

    @BeforeEach
    void setUp() {
        sut = new DtoValidator(validator);
    }

    @Test
    void validate_whenNoViolations_emitsTargetAndCompletes() {
        var dto = new Object();

        when(validator.validate(dto)).thenReturn(Collections.emptySet());

        StepVerifier.create(sut.validate(dto))
                .expectNext(dto)
                .verifyComplete();

        verify(validator).validate(dto);
        verifyNoMoreInteractions(validator);
    }

    @Test
    void validate_whenViolations_emitsConstraintViolationException() {
        var dto = new Object();

        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Set<ConstraintViolation<Object>> violations = Set.of(violation);

        when(validator.validate(dto)).thenReturn(violations);

        StepVerifier.create(sut.validate(dto))
                .expectError(ConstraintViolationException.class)
                .verify();

        verify(validator).validate(dto);
        verifyNoMoreInteractions(validator);
    }
}
