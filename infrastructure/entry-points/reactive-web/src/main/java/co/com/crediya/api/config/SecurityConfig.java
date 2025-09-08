package co.com.crediya.api.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ConverterJwtToAuth jwtAuthConverter
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                .authorizeExchange(ex -> ex
                        .pathMatchers("/webjars/swagger-ui/**", "/v3/api-docs/**",
                                "/actuator/health", "/.well-known/jwks.json").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/login",
                                "/api/v1/login").permitAll()

                        // Users
                        .pathMatchers(HttpMethod.GET, "/api/users")
                        .hasAnyRole("ADMIN", "ADVISOR")
                        .pathMatchers(HttpMethod.POST, "/api/users")
                        .hasAnyRole("ADMIN", "ADVISOR")

                        .pathMatchers(HttpMethod.GET, "/api/users/*")
                        .authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/users/*")
                        .hasAnyRole("ADMIN", "ADVISOR")

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                jwtAuthConverter)))
                .build();
    }

    @Bean
    ConverterJwtToAuth jwtAuthConverter() {
        return new ConverterJwtToAuth("roles", "ROLE_");
    }

    public static class ConverterJwtToAuth implements
            org.springframework.core.convert.converter.Converter<Jwt, Mono<AbstractAuthenticationToken>> {
        private final String authoritiesClaim;

        private final String prefix;

        public ConverterJwtToAuth(String authoritiesClaim, String prefix) {
            this.authoritiesClaim = authoritiesClaim;
            this.prefix = prefix;
        }

        @Override
        public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
            var granted = new JwtGrantedAuthoritiesConverter();
            granted.setAuthoritiesClaimName(authoritiesClaim);
            granted.setAuthorityPrefix(prefix);

            var delegate = new JwtAuthenticationConverter();
            delegate.setJwtGrantedAuthoritiesConverter(granted);

            return new ReactiveJwtAuthenticationConverterAdapter(
                    delegate).convert(jwt);
        }
    }
}
