package in.bm.AuthService.SERVICE;

import com.twilio.exception.ApiException;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import in.bm.AuthService.DTO.*;
import in.bm.AuthService.ENTITY.AuthUser;
import in.bm.AuthService.ENTITY.Provider;
import in.bm.AuthService.EXCEPTION.InvalidOtpException;
import in.bm.AuthService.EXCEPTION.OtpSendException;
import in.bm.AuthService.REPOSITORY.AuthUserRepo;
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
            throw new OtpSendException("Unable to send OTP");
        }
    }

    public VerifyOtpResponse verifyOtp(@Valid VerifyOtpRequest otpRequest, HttpServletResponse response) {
        try {
            VerificationCheck check = VerificationCheck.creator(serviceSid)
                    .setTo(otpRequest.getPhoneNumber())
                    .setCode(otpRequest.getOtp())
                    .create();

            if (!"approved".equals(check.getStatus())) {
                throw new InvalidOtpException("Invalid or expired OTP");
            }

            AuthUser user = authUserRepo.findByPhoneNumber(otpRequest.getPhoneNumber())
                    .orElseThrow(() -> new IllegalStateException("User not found"));

            String accessToken = jwtService.generateAccessToken(user.getId());
            String refreshToken = jwtService.generateRefreshToken(user.getId());

            Cookie c = new Cookie("refresh-token", refreshToken);
            c.setMaxAge(30 * 24 * 60 * 60);//30 days
            c.setSecure(true);
            c.setPath("/");
            c.setHttpOnly(true);
            response.addCookie(c);

            return VerifyOtpResponse.builder()
                    .token(accessToken)
                    .tokenType(TOKEN_TYPE)
                    .build();

        } catch (ApiException ex) {
            log.error("OTP verification failed", ex);
            throw new OtpSendException("Unable to verify OTP");
        }
    }

    public AuthResponse googleLogin(OauthRequestDTO requestDTO,
                                    HttpServletResponse response) {

        GoogleUserInfo googleUser = googleTokenVerifier.verify(requestDTO.getIdToken());

       AuthUser user = authUserRepo
                .findByProviderAndEmail
                        (Provider.GOOGLE, googleUser.getEmail())
                .orElseGet(() -> createUser(googleUser));

       String accessToken = jwtService.generateAccessToken(user.getId());
       String refreshToken  = jwtService.generateRefreshToken(user.getId());

       Cookie c = new Cookie("refresh_token",refreshToken);
        c.setMaxAge(30 * 24 * 60 * 60);//30 days
        c.setSecure(true);
        c.setPath("/");
        c.setHttpOnly(true);
       response.addCookie(c);

       return AuthResponse.builder().token(accessToken).tokenType(TOKEN_TYPE).build();

    }

    private AuthUser createUser(GoogleUserInfo googleUserInfo) {
        AuthUser user = new AuthUser();
        user.setEmail(googleUserInfo.getEmail());
        user.setProvider(Provider.GOOGLE);
        user.setCreatedAt(Instant.now());
        return authUserRepo.save(user);
    }


}
