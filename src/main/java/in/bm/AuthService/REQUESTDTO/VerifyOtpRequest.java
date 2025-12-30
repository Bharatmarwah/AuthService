package in.bm.AuthService.REQUESTDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class VerifyOtpRequest {

    @NotBlank
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "OTP must be exactly 6 digits"
    )
    private String otp;

    @NotBlank
    @Pattern(
            regexp = "^\\+[1-9]\\d{1,14}$",
            message = "Invalid phone number format"
    )
    private String phoneNumber;

}
