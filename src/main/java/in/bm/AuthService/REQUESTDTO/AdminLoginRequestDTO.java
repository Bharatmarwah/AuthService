package in.bm.AuthService.REQUESTDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLoginRequestDTO {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
