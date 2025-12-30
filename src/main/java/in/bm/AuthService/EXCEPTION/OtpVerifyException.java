package in.bm.AuthService.EXCEPTION;

import com.twilio.exception.ApiException;

public class OtpVerifyException extends AuthException {
    public OtpVerifyException(String message, Throwable cause){
        super(message);
    }
}
