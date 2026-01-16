package in.bm.AuthService.CONFIGURATION;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;

@Configuration
public class JwtDecoderConfig {

    private static final String secretKey =
            "YjkxYTczZDgzN2I2NDk4N2MxZWMyNGUwODZjYmY4YzIyZjQzMDE5OWE2NmQ2N2E5M2Q0MzFjYTFmYzU0MGI3MQ==";

    @Bean
    public JwtDecoder jwtDecoder() {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
