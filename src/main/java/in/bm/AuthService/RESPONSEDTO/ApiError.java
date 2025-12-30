package in.bm.AuthService.RESPONSEDTO;

import java.time.LocalDateTime;

public record ApiError(
        String error,
        String message,
        int status,
        LocalDateTime timestamp
) {}
