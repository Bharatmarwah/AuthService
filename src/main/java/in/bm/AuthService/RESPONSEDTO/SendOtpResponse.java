package in.bm.AuthService.RESPONSEDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendOtpResponse {
    private boolean otpSent;
}
