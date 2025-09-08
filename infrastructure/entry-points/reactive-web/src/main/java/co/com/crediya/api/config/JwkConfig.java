package co.com.crediya.api.config;

import co.com.crediya.model.user.gateways.TokenProvider;
import java.security.NoSuchAlgorithmException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
class JwkConfig {

    @Bean
    RSAKey rsaKey() throws NoSuchAlgorithmException {
        var kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        var pair = kpg.generateKeyPair();
        return new RSAKey.Builder(
                (java.security.interfaces.RSAPublicKey) pair.getPublic())
                .privateKey(pair.getPrivate())
                .keyID(java.util.UUID.randomUUID().toString())
                .build();
    }

    @Bean
    JWKSource<SecurityContext> jwkSource(RSAKey rsa) {
        return (selector, ctx) -> selector.select(
                new com.nimbusds.jose.jwk.JWKSet(rsa));
    }

    @Bean
    JwtEncoder jwtEncoder(JWKSource<SecurityContext> source) {
        return new org.springframework.security.oauth2.jwt.NimbusJwtEncoder(
                source);
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder(RSAKey rsa) throws JOSEException {
        return org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
                .withPublicKey(rsa.toRSAPublicKey())
                .build();
    }

    @Bean
    TokenProvider tokenProvider(JwtEncoder encoder) {
        return (userId, email, name, roles, ttl) -> {
            var now = java.time.Instant.now();
            var claims =
                    org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
                            .subject(userId.toString())
                            .claim("email", email.value())
                            .claim("name", name)
                            .claim("roles", roles.stream()
                                    .map(co.com.crediya.model.role.Role::name)
                                    .toList())
                            .issuedAt(now)
                            .expiresAt(now.plus(ttl))
                            .build();
            var header = org.springframework.security.oauth2.jwt.JwsHeader
                    .with(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256) // ⬅️ RSA
                    .build();
            return encoder.encode(
                            org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(
                                    header, claims))
                    .getTokenValue();
        };
    }
}
