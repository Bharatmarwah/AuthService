package in.bm.AuthService.SERVICE;

import in.bm.AuthService.EXCEPTION.OauthAuthenticationException;
import in.bm.AuthService.RESPONSEDTO.GoogleUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class GoogleTokenVerifier {

    private final JwtDecoder jwtDecoder;

    @Value("${google.oauth.client-id}")
    private String clientId;


    public GoogleUserInfo verify(String idToken) {
        Jwt jwt = jwtDecoder.decode(idToken);

        Instant expiresAt = jwt.getExpiresAt();

        if (expiresAt!=null && expiresAt.isBefore(Instant.now())){
            throw new OauthAuthenticationException("Token expired");
        }

        if (!jwt.getAudience().contains(clientId)){
            throw new OauthAuthenticationException("Invalid token audience");
        }

        Boolean emailVerified = jwt.getClaim("email_verified");
        if (emailVerified == null || !emailVerified){
            throw new OauthAuthenticationException("Email not verified by Google");
        }


        return GoogleUserInfo
                .builder()
                .name(jwt.getClaim("name"))
                .email(jwt.getClaim("email"))
                .subject(jwt.getSubject())
                .build();
    }


}
