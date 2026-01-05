package in.bm.AuthService.RESPONSEDTO;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CreateAdminResponseDTO {
    private UUID adminId;
    private String message;
}
