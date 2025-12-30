package in.bm.AuthService.SERVICE;

import in.bm.AuthService.DTO.GoogleUserInfo;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.stereotype.Component;

@Component
public class GoogleTokenVerifier {

    private final JwtDecoder jwtDecoder;

    public GoogleTokenVerifier(){
        this.jwtDecoder= JwtDecoders
                .fromIssuerLocation
                        ("https://accounts.google.com");// Google public key for verification
    }

    public GoogleUserInfo verify(String idToken){
        Jwt jwt = jwtDecoder.decode(idToken);

        return GoogleUserInfo
                .builder()
                .name(jwt.getClaim("name"))
                .email(jwt.getClaim("email"))
                .subject(jwt.getSubject())
                .build();
    }



}
