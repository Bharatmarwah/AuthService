package in.bm.AuthService.SERVICE;

import com.twilio.exception.ApiException;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import in.bm.AuthService.ENTITY.*;
import in.bm.AuthService.EXCEPTION.*;
import in.bm.AuthService.REPOSITORY.AuthAdminRepo;
import in.bm.AuthService.REPOSITORY.AuthUserRepo;
import in.bm.AuthService.REPOSITORY.RefreshTokenRepo;
import in.bm.AuthService.REQUESTDTO.*;
import in.bm.AuthService.RESPONSEDTO.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    @Value("${twilio.verify.service-sid}")
    private String serviceSid;

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.client-secret}")
    private String clientSecret;

    public static final String TOKEN_TYPE = "Bearer";

    private final AuthUserRepo authUserRepo;
    private final AuthAdminRepo authAdminRepo;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenRepo refreshTokenRepo;


    @Async
    public void sendOtp(@Valid SendOtpRequest request) {

        try {
            Verification.creator(
                    serviceSid,
                    request.getPhoneNumber(),
                    "sms"
            ).create();

            // Create new user or leave
            authUserRepo.findByPhoneNumber(request.getPhoneNumber())
                    .orElseGet(() -> {
                        AuthUser user = new AuthUser();
                        user.setPhoneNumber(request.getPhoneNumber());
                        user.setProvider(Provider.OTP);
                        user.setCreatedAt(Instant.now());
                        user.setRole(Role.ROLE_USER);
                        return authUserRepo.save(user);
                    });


        } catch (ApiException ex) {
            log.error("OTP send failed", ex);
            throw new OtpSendException("Unable to send OTP", ex);
        }
    }

    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request, HttpServletResponse response) {

        VerificationCheck check = VerificationCheck.creator(serviceSid)
                .setTo(request.getPhoneNumber())
                .setCode(request.getOtp())
                .create();

        if (!"approved".equalsIgnoreCase(check.getStatus())) {
            throw new RuntimeException("Invalid OTP");
        }

        AuthUser user = authUserRepo.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("User not found"));

        handleLogin(user, response);

        String accessToken = jwtService.generateAccessToken(
                user.getUserId().toString(),
                user.getRole().toString()
        );

        return VerifyOtpResponse.builder()
                .token(accessToken)
                .tokenType(TOKEN_TYPE)
                .build();
    }


    @Transactional
    public AuthResponse googleLogin(OauthRequestDTO request, HttpServletResponse response) {

        GoogleUserInfo googleUser = googleTokenVerifier.verify(request.getIdToken());

        AuthUser user = authUserRepo
                .findByProviderAndEmail(Provider.GOOGLE, googleUser.getEmail())
                .orElseGet(() -> createGoogleUser(googleUser));

        handleLogin(user, response);

        String accessToken = jwtService.generateAccessToken(
                user.getUserId().toString(),
                user.getRole().toString()
        );

        return AuthResponse.builder()
                .token(accessToken)
                .tokenType(TOKEN_TYPE)
                .build();
    }

    public GoogleResponse exchangeAuthorizationCode(String authorizationCode) {

        String googleTokenEndpoint =
                "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> tokenRequestBody =
                new LinkedMultiValueMap<>();

        tokenRequestBody.add("code", authorizationCode);
        tokenRequestBody.add("client_id", clientId);
        tokenRequestBody.add("client_secret", clientSecret);
        tokenRequestBody.add(
                "redirect_uri",
                "http://localhost:8080/public/oauth/callback"
        );
        tokenRequestBody.add(
                "grant_type",
                "authorization_code"
        );

        HttpHeaders requestHeaders = new HttpHeaders();

        requestHeaders.setContentType(
                MediaType.APPLICATION_FORM_URLENCODED
        );

        HttpEntity<MultiValueMap<String, String>> tokenRequest =
                new HttpEntity<>(
                        tokenRequestBody,
                        requestHeaders
                );

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> tokenResponse =
                restTemplate.postForEntity(
                        googleTokenEndpoint,
                        tokenRequest,
                        Map.class
                );

        Map<String, Object> tokenResponseBody =
                tokenResponse.getBody();

        String idToken =
                (String) tokenResponseBody.get("id_token");

        return GoogleResponse.builder()
                .idToken(idToken)
                .build();
    }

    public void loginWithGoogle(HttpServletResponse response) throws IOException {
        String scope = "openid email profile";

        String authUrl =
                "https://accounts.google.com/o/oauth2/v2/auth?" +
                        "client_id=" + clientId +
                        "&redirect_uri=http://localhost:8080/public/oauth/callback" +
                        "&response_type=code" +
                        "&scope=" + scope +
                        "&access_type=offline" +
                        "&prompt=consent";

        response.sendRedirect(authUrl);
    }

    // ================= HANDLE LOGIN =================

    private void handleLogin(AuthUser user, HttpServletResponse response) {

        refreshTokenRepo.revokeAll(user);

        String refreshToken = jwtService.generateRefreshToken(
                user.getUserId().toString(),
                user.getRole().toString()
        );

        saveRefreshToken(user, refreshToken);
        addRefreshCookie(response, refreshToken);
    }

    // ================= SAVE TOKEN =================

    private void saveRefreshToken(AuthUser user, String refreshToken) {

        String hash = jwtService.getTokenHash(refreshToken);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setRefreshTokenHash(hash);
        token.setUsed(false);
        token.setRevoked(false);
        token.setCreatedAt(Instant.now());
        token.setExpiryAt(Instant.now().plus(30, ChronoUnit.DAYS));

        refreshTokenRepo.save(token);
    }

    // ================= REFRESH =================

    @Transactional
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String refreshToken = extractRefreshToken(request);

        String hash = jwtService.getTokenHash(refreshToken);

        RefreshToken token = refreshTokenRepo.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        AuthUser user = token.getUser();

        if (token.isUsed()) {
            refreshTokenRepo.revokeAll(user);
            throw new RuntimeException("Reuse detected. All sessions revoked.");
        }

        if (token.isRevoked()) {
            throw new RuntimeException("Token revoked");
        }

        if (token.getExpiryAt().isBefore(Instant.now())) {
            throw new RuntimeException("Token expired");
        }

        token.setUsed(true);
        refreshTokenRepo.save(token);

        String newAccessToken = jwtService.generateAccessToken(
                user.getUserId().toString(),
                user.getRole().toString()
        );

        String newRefreshToken = jwtService.generateRefreshToken(
                user.getUserId().toString(),
                user.getRole().toString()
        );

        saveRefreshToken(user, newRefreshToken);
        addRefreshCookie(response, newRefreshToken);

        return AuthResponse.builder()
                .token(newAccessToken)
                .tokenType(TOKEN_TYPE)
                .build();
    }

    // ================= COOKIE =================

    private void addRefreshCookie(HttpServletResponse response, String refreshToken) {

        Cookie cookie = new Cookie("refresh-token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60);

        response.addCookie(cookie);
    }

    private String extractRefreshToken(HttpServletRequest request) {

        if (request.getCookies() == null) {
            throw new RuntimeException("No cookies");
        }

        for (Cookie cookie : request.getCookies()) {
            if ("refresh-token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new RuntimeException("Refresh token missing");
    }


    private AuthUser createGoogleUser(GoogleUserInfo info) {

        AuthUser user = new AuthUser();
        user.setEmail(info.getEmail());
        user.setProvider(Provider.GOOGLE);
        user.setCreatedAt(Instant.now());
        user.setRole(Role.ROLE_USER);

        return authUserRepo.save(user);
    }


}

