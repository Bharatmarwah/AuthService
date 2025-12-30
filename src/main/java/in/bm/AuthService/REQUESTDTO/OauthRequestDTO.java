package in.bm.AuthService.REQUESTDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OauthRequestDTO {

    @NotBlank
    private String idToken;


}
