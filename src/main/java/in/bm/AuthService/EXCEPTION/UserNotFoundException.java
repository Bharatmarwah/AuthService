package in.bm.AuthService.EXCEPTION;

public class UserNotFoundException extends AuthException{
    public UserNotFoundException(String message) {
        super(message);
    }
}
