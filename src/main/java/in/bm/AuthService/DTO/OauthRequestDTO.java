package in.bm.AuthService.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OauthRequestDTO {

    @NotBlank
    private String idToken;


}
