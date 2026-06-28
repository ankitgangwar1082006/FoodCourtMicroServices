package auth_service.com.auth_service.Dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDto {
    private Long id;
    private String msg;
    private String token;
}
