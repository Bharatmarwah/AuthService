package in.bm.AuthService.SERVICE;

import in.bm.AuthService.ENTITY.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private static final String
            secretKey = "YjkxYTczZDgzN2I2NDk4N2MxZWMyNGUwODZjYmY4YzIyZjQzMDE5OWE2NmQ2N2E5M2Q0MzFjYTFmYzU0MGI3MQ==";


    private static final long Access_Token_Validity = 60 * 3 * 1000l;
    private static final long Refresh_Token_Validity =  30L * 24 * 60 * 60 * 1000;



    public String generateAccessToken(UUID userId , Role role){
        return Jwts.builder()
                .issuer("kitflik-auth-service")
                .claim("type","ACCESS")
                .claim("role",role)
                .setSubject(userId.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+Access_Token_Validity))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UUID userId, Role role){
        return Jwts.builder()
                .issuer("kitflik-auth-service")
                .claim("type","REFRESH")
                .claim("role",role)
                .setSubject(userId.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+Refresh_Token_Validity))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}