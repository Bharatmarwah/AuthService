package in.bm.AuthService.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendOtpResponse {
    private boolean otpSent;
}
