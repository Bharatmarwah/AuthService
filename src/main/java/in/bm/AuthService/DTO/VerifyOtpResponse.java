package in.bm.AuthService.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyOtpResponse {
    private String token;
    private String tokenType;
}
