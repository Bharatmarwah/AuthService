package in.bm.AuthService.CONTROLLER;

import in.bm.AuthService.REQUESTDTO.*;
import in.bm.AuthService.RESPONSEDTO.AuthResponse;
import in.bm.AuthService.RESPONSEDTO.SendOtpResponse;
import in.bm.AuthService.RESPONSEDTO.VerifyOtpResponse;
import in.bm.AuthService.SERVICE.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //localhost:8080/auth/otp/send
    @PostMapping("/otp/send")
    public ResponseEntity<SendOtpResponse> sendOtp(@Valid
                                                   @RequestBody
                                                   SendOtpRequest sendOtpRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.sendOtp(sendOtpRequest));
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

    //localhost:8080/auth/oauth/google
    @PostMapping("/oauth/google")
    public ResponseEntity<AuthResponse> googleAuth(@RequestBody OauthRequestDTO requestDTO, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.googleLogin(requestDTO, response));
    }


    //localhost:8080/auth/admin/login
    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> adminLogin(@Valid @RequestBody AdminLoginRequestDTO requestDTO, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.adminLogin(requestDTO, response));
    }

    @GetMapping("test")
    public String test() {
        return "working";
    }


}
