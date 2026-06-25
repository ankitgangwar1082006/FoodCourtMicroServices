package auth_service.com.auth_service.controller;

import auth_service.com.auth_service.Dto.request.AuthRequestDto;
import auth_service.com.auth_service.Dto.request.UserRequestDto;
import auth_service.com.auth_service.Dto.response.AuthResponseDto;
import auth_service.com.auth_service.Dto.response.UserResponseDto;
import auth_service.com.auth_service.security.JwtService;
import auth_service.com.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    // Helper method to extract token and get User ID
    private Long getLoggedInUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // "Bearer " hataya
            return jwtService.getUserId(token); // Token se ID nikali
        }
        throw new RuntimeException("Security Alert: Missing or Invalid Authorization token!");
    }

    // 1. REGISTER (Token not required)
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRequestDto dto) {
        return new ResponseEntity<>(authService.registerUser(dto), HttpStatus.CREATED);
    }

    // 2. LOGIN (Token not required)
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody AuthRequestDto dto) {
        return ResponseEntity.ok(authService.validateUser(dto));
    }

    // 3. GET USER
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long loggedInUserId = getLoggedInUserId(authHeader);
        return ResponseEntity.ok(authService.getUserById(id, loggedInUserId));
    }

    // 4. UPDATE USER
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDto dto,
            @RequestHeader("Authorization") String authHeader) {

        Long loggedInUserId = getLoggedInUserId(authHeader);
        return ResponseEntity.ok(authService.updateUser(id, dto, loggedInUserId));
    }

    // 5. DELETE USER
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long loggedInUserId = getLoggedInUserId(authHeader);
        authService.deleteUser(id, loggedInUserId);
        return ResponseEntity.ok("User deleted successfully!");
    }
}