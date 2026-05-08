package in.bm.AuthService.RESPONSEDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GoogleResponse {

    @JsonProperty("id-token")
    private String idToken;

}
