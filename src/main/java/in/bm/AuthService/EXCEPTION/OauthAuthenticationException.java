package in.bm.AuthService.EXCEPTION;

public class OauthAuthenticationException extends AuthException
{
    public OauthAuthenticationException(String message, Throwable cause) {
        super(message,cause);
    }

    public OauthAuthenticationException(String message){
        super(message);
    }
}
