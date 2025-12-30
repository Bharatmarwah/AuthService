package in.bm.AuthService.RESPONSEDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private String token;
    private String tokenType;

}
