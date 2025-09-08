package co.com.crediya.api.config;

import co.com.crediya.model.user.gateways.PasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
class CryptoConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(10); } // ~1s

    @Bean
    PasswordHasher passwordHasher(PasswordEncoder pe) {
        return new PasswordHasher() {
            public String hash(String raw) { return pe.encode(raw); }
            public boolean matches(String raw, String hash) { return pe.matches(raw, hash); }
        };
    }
}
