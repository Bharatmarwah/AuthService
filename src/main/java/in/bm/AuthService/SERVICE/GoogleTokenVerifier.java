package in.bm.AuthService.SERVICE;

import in.bm.AuthService.EXCEPTION.OauthAuthenticationException;
import in.bm.AuthService.RESPONSEDTO.GoogleUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.stereotype.Component;

@Component
public class GoogleTokenVerifier {
    private final JwtDecoder jwtDecoder;

    private final String clientId;

    public GoogleTokenVerifier(@Value("${client-id}") String clientId) {
        this.jwtDecoder = JwtDecoders
                .fromIssuerLocation
                        ("https://accounts.google.com");// Google public key for verification

        this.clientId = clientId;
    }

    public GoogleUserInfo verify(String idToken) {
        Jwt jwt = jwtDecoder.decode(idToken);

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
