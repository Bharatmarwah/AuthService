package in.bm.AuthService.RESPONSEDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyOtpResponse {
    private String token;
    private String tokenType;
}
