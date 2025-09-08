package co.com.crediya.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class JwksController {
    private final com.nimbusds.jose.jwk.JWKSet jwkSet;
    JwksController(com.nimbusds.jose.jwk.RSAKey rsa) { this.jwkSet = new com.nimbusds.jose.jwk.JWKSet(rsa.toPublicJWK()); }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> keys() { return jwkSet.toJSONObject(); }
}
