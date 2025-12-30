package in.bm.AuthService.RESPONSEDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoogleUserInfo {

    private String subject;
    private String email;
    private String name;

}
