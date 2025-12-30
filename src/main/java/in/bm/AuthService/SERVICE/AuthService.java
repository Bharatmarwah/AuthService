package in.bm.AuthService.SERVICE;

import com.twilio.exception.ApiException;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import in.bm.AuthService.ENTITY.AuthUser;
import in.bm.AuthService.ENTITY.Provider;
import in.bm.AuthService.EXCEPTION.*;
import in.bm.AuthService.REPOSITORY.AuthUserRepo;
import in.bm.AuthService.REQUESTDTO.*;
import in.bm.AuthService.RESPONSEDTO.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    @Value("${twilio.verify.service-sid}")
    private String serviceSid;

    public static final String TOKEN_TYPE = "Bearer";

    private final AuthUserRepo authUserRepo;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;



    public SendOtpResponse sendOtp(@Valid SendOtpRequest request) {

        try {
            Verification.creator(
                    serviceSid,
                    request.getPhoneNumber(),
                    "sms"
            ).create();

            authUserRepo.findByPhoneNumber(request.getPhoneNumber())
                    .orElseGet(() -> {
                        AuthUser user = new AuthUser();
                        user.setPhoneNumber(request.getPhoneNumber());
                        user.setProvider(Provider.OTP);
                        user.setCreatedAt(Instant.now());
                        return authUserRepo.save(user);
                    });

            return SendOtpResponse.builder()
                    .otpSent(true)
                    .build();

        } catch (ApiException ex) {
            log.error("OTP send failed", ex);
            throw new OtpSendException("Unable to send OTP", ex);
        }
    }


    public VerifyOtpResponse verifyOtp(
            @Valid VerifyOtpRequest request,
            HttpServletResponse response
    ) {

        try {
            VerificationCheck check = VerificationCheck.creator(serviceSid)
                    .setTo(request.getPhoneNumber())
                    .setCode(request.getOtp())
                    .create();

            if (!"approved".equalsIgnoreCase(check.getStatus())) {
                throw new InvalidOtpException("Invalid or expired OTP");
            }

            AuthUser user = authUserRepo.findByPhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            String accessToken = jwtService.generateAccessToken(user.getId());
            String refreshToken = jwtService.generateRefreshToken(user.getId());

            addRefreshCookie(response, refreshToken);

            return VerifyOtpResponse.builder()
                    .token(accessToken)
                    .tokenType(TOKEN_TYPE)
                    .build();

        } catch (InvalidOtpException | UserNotFoundException ex) {
            throw ex;
        } catch (ApiException ex) {
            log.error("OTP verification failed", ex);
            throw new OtpVerifyException("OTP verification failed", ex);
        }
    }


    public AuthResponse googleLogin(
            OauthRequestDTO requestDTO,
            HttpServletResponse response
    ) {

        try {
            GoogleUserInfo googleUser =
                    googleTokenVerifier.verify(requestDTO.getIdToken());

            AuthUser user = authUserRepo
                    .findByProviderAndEmail(Provider.GOOGLE, googleUser.getEmail())
                    .orElseGet(() -> createGoogleUser(googleUser));

            String accessToken = jwtService.generateAccessToken(user.getId());
            String refreshToken = jwtService.generateRefreshToken(user.getId());

            addRefreshCookie(response, refreshToken);

            return AuthResponse.builder()
                    .token(accessToken)
                    .tokenType(TOKEN_TYPE)
                    .build();

        } catch (OauthAuthenticationException ex) {
            log.error("Google OAuth failed", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected OAuth error", ex);
            throw new OauthAuthenticationException("Google authentication failed", ex);
        }
    }


    private AuthUser createGoogleUser(GoogleUserInfo info) {
        AuthUser user = new AuthUser();
        user.setEmail(info.getEmail());
        user.setProvider(Provider.GOOGLE);
        user.setCreatedAt(Instant.now());
        return authUserRepo.save(user);
    }

    private void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refresh-token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
        response.addCookie(cookie);
    }
}
