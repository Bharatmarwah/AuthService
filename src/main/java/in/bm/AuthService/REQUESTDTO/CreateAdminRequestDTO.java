package in.bm.AuthService.REQUESTDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateAdminRequestDTO {

    @NotBlank
    private String username;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$",
            message = "Password must be at least 12 chars, with upper, lower, number & special char"
    )
    private String password;
}
