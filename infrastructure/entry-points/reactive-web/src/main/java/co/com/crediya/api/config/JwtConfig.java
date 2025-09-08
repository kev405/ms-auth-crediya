//package co.com.crediya.api.config;
//
//import java.nio.charset.StandardCharsets;
//import java.time.Instant;
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//
//import com.nimbusds.jose.JWSAlgorithm;
//import com.nimbusds.jose.jwk.JWKSet;
//import com.nimbusds.jose.jwk.OctetSequenceKey;
//import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
//import com.nimbusds.jose.jwk.source.JWKSource;
//import com.nimbusds.jose.proc.SecurityContext;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
//import org.springframework.security.oauth2.jwt.*;
//import co.com.crediya.model.role.Role;
//import co.com.crediya.model.user.gateways.TokenProvider;
//
//@Configuration
//class JwtConfig {
//
//    @Value("${security.jwt.secret}")
//    String secret;
//
//    @Bean
//    SecretKey hmacKey() {
//        // Usa al menos 32 bytes para HS256 (>= 256 bits)
//        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//    }
//
//    @Bean
//    JwtEncoder jwtEncoder(SecretKey hmacKey) {
//        var jwk = new OctetSequenceKey.Builder(hmacKey)
//                .algorithm(JWSAlgorithm.HS256)
//                .build();
//        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
//        return new NimbusJwtEncoder(jwkSource);
//    }
//
//    @Bean
//    ReactiveJwtDecoder jwtDecoder(SecretKey hmacKey) {
//        return NimbusReactiveJwtDecoder.withSecretKey(hmacKey)
//                .macAlgorithm(MacAlgorithm.HS256)
//                .build();
//    }
//
//    @Bean
//    TokenProvider tokenProvider(JwtEncoder encoder) {
//        return (userId, email, roles, ttl) -> {
//            var now = Instant.now();
//            var claims = JwtClaimsSet.builder()
//                    .subject(userId.toString())
//                    .claim("email", email.value())
//                    .claim("roles", roles.stream().map(Role::name).toList())
//                    .issuedAt(now)
//                    .expiresAt(now.plus(ttl))
//                    .build();
//            var header = JwsHeader.with(MacAlgorithm.HS256).build();
//            return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
//        };
//    }
//}