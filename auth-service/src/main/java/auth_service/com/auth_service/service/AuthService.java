package auth_service.com.auth_service.service;

import auth_service.com.auth_service.Dto.request.AuthRequestDto;
import auth_service.com.auth_service.Dto.request.UserRequestDto;
import auth_service.com.auth_service.Dto.response.AuthResponseDto;
import auth_service.com.auth_service.Dto.response.UserResponseDto;
import auth_service.com.auth_service.entity.User;
import auth_service.com.auth_service.entity.UserProfile;
import auth_service.com.auth_service.repository.UserRepository;
import auth_service.com.auth_service.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional(rollbackOn = Exception.class)
    public UserResponseDto registerUser(UserRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered!");
        }
        UserProfile userProfile = createUserProfile(dto);
        User user = createUser(dto);

        user.setProfile(userProfile);
        userProfile.setUser(user);

        User saveduser = userRepository.save(user);
        return mapToResponseDto(saveduser);
    }

    public UserResponseDto getUserById(Long id, Long loggedInUserId) {
        validateUserAccess(id, loggedInUserId); // Security Check (IDOR Protection)

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        return mapToResponseDto(user);
    }

    @Transactional(rollbackOn = Exception.class)
    public UserResponseDto updateUser(Long id, UserRequestDto dto, Long loggedInUserId) {
        validateUserAccess(id, loggedInUserId); // Security Check (IDOR Protection)

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));


        existingUser.setName(dto.getName());

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        UserProfile profile = existingUser.getProfile();

        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(existingUser);
            existingUser.setProfile(profile);
        }

        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setAddress(dto.getAddress());
        profile.setGender(dto.getGender());

        User updatedUser = userRepository.save(existingUser);
        return mapToResponseDto(updatedUser);
    }

    @Transactional(rollbackOn = Exception.class)
    public void deleteUser(Long id, Long loggedInUserId) {
        validateUserAccess(id, loggedInUserId); // Security Check (IDOR Protection)

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    public AuthResponseDto validateUser(AuthRequestDto dto) {
        String userName = dto.getEmail();
        String pswd = dto.getPassword();

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, pswd));
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = userRepository.findByEmail(userName)
                .orElseThrow(() -> new RuntimeException("Invalid or wrong email"));

        String token = jwtService.generateToken(dto.getEmail(), user.getId(), user.getRole());

        return createAuthResponse(token, user.getId());
    }


    private void validateUserAccess(Long targetId, Long loggedInUserId) {
        if (!targetId.equals(loggedInUserId)) {
            throw new RuntimeException("Security Alert: Unauthorized access! You can only access or modify your own profile.");
        }
    }

    private User createUser(UserRequestDto dto) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .build();
    }

    private UserProfile createUserProfile(UserRequestDto dto) {
        return UserProfile.builder()
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .gender(dto.getGender())
                .build();
    }

    private AuthResponseDto createAuthResponse(String token, Long id) {
        return AuthResponseDto.builder().token(token).id(id).msg("login success!").build();
    }

    private UserResponseDto mapToResponseDto(User user) {
        String phone = user.getProfile() != null ? user.getProfile().getPhoneNumber() : null;
        String address = user.getProfile() != null ? user.getProfile().getAddress() : null;
        String gender = user.getProfile() != null ? user.getProfile().getGender() : null;
        String picUrl = user.getProfile() != null ? user.getProfile().getProfilePictureUrl() : null;

        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phoneNumber(phone)
                .address(address)
                .gender(gender)
                .profilePictureUrl(picUrl)
                .build();
    }
}