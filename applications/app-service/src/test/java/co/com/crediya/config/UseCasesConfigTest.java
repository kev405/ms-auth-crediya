package co.com.crediya.config;

import co.com.crediya.model.user.gateways.PasswordHasher;
import co.com.crediya.model.user.gateways.TokenProvider;
import co.com.crediya.model.user.gateways.TxRunner;
import co.com.crediya.model.user.gateways.UserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import java.util.function.Supplier;

public class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'Use Case' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {

        @Bean
        public UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        public PasswordHasher passwordHasher() {
            return mock(PasswordHasher.class);
        }

        @Bean
        public TokenProvider tokenProvider() {
            return mock(TokenProvider.class);
        }

        @Bean
        public TxRunner txRunner() {
            return new TxRunner() {
                @Override
                public <T> Mono<T> required(Supplier<Mono<T>> work) {
                    return work.get();
                }

                @Override
                public <T> Flux<T> requiredMany(Supplier<Flux<T>> action) {
                    return null;
                }

                @Override
                public <T> Mono<T> readOnly(Supplier<Mono<T>> work) {
                    return work.get();
                }

                @Override
                public <T> Flux<T> readOnlyMany(Supplier<Flux<T>> action) {
                    return TxRunner.super.readOnlyMany(action);
                }
            };
        }
    }
}