package in.bm.AuthService.EXCEPTION;

public class InvalidOtpException extends AuthException{
    public InvalidOtpException(String message) {
        super(message);
    }
}
