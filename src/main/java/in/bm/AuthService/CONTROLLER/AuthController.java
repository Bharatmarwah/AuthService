package in.bm.AuthService.CONTROLLER;

import in.bm.AuthService.REQUESTDTO.*;
import in.bm.AuthService.RESPONSEDTO.AuthResponse;
import in.bm.AuthService.RESPONSEDTO.GoogleResponse;
import in.bm.AuthService.RESPONSEDTO.VerifyOtpResponse;
import in.bm.AuthService.SERVICE.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class AuthController {

    @Value("${google.oauth.client-id}")
    private String clientId;


    private final AuthService authService;

    //localhost:8080/auth/otp/send
    //Phone number login request
    @PostMapping("/otp/send")
    public ResponseEntity<Void> sendOtp(@Valid
                                                   @RequestBody
                                                   SendOtpRequest sendOtpRequest) {
        authService.sendOtp(sendOtpRequest);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //localhost:8080/auth/otp/verify
    @PostMapping("/otp/verify")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@Valid
                                                       @RequestBody
                                                       VerifyOtpRequest verifyOtpRequest, HttpServletResponse response) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.verifyOtp(verifyOtpRequest, response));
    }

    @GetMapping("/google/login")
    public void login(HttpServletResponse response) throws IOException {
        authService.loginWithGoogle(response);
    }

    @GetMapping(value = "/oauth/callback",produces = MediaType.APPLICATION_JSON_VALUE)
    public GoogleResponse exchangeAuthenticationCode(@RequestParam(name = "code") String authenticationCode){
        return authService.exchangeAuthorizationCode(authenticationCode);
    }

    //localhost:8080/auth/oauth/google
    @PostMapping("/oauth/google")
    public ResponseEntity<AuthResponse> googleAuth(@RequestBody OauthRequestDTO requestDTO, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.googleLogin(requestDTO, response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request,HttpServletResponse response) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(authService.refreshToken(request , response));
    }



}
