package auth_service.com.auth_service.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private String role;

    private String phoneNumber;
    private String address;
    private String gender;

    private String profilePictureUrl;
}