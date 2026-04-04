package in.bm.AuthService.SERVICE;


import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    private final String privateKey;

    public JwtService(@Value("${jwt.private.key}") String privateKey) {
        this.privateKey = privateKey;
    }

    private static final long Access_Token_Validity = 60 * 3 * 1000L;
    private static final long Refresh_Token_Validity = 30L * 24 * 60 * 60 * 1000L;


    public String generateAccessToken(String userId, String role) {
        return Jwts.builder()
                .issuer("kitflik-auth-service")
                .claim("type", "ACCESS")
                .claim("role", role)
                .subject(userId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + Access_Token_Validity))
                .signWith(privateKey(),Jwts.SIG.RS256)
                .compact();
    }

    public String generateRefreshToken(String userId, String role) {
        return Jwts.builder()
                .issuer("kitflik-auth-service")
                .claim("type", "REFRESH")
                .claim("role", role)
                .subject(userId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + Refresh_Token_Validity))
                .signWith(privateKey(),Jwts.SIG.RS256)
                .compact();
    }

    private PrivateKey privateKey() {
        try {
            byte[] keyByte = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyByte);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    public String getTokenHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA_256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        }catch (Exception e){
            throw new RuntimeException("Failed to refresh token", e);
        }
    }
}