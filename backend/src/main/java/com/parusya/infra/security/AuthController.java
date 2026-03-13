package com.parusya.infra.security;

import com.parusya.infra.exception.BusinessException;
import com.parusya.infra.security.dto.AuthDtos.LoginRequest;
import com.parusya.infra.security.dto.AuthDtos.LoginResponse;
import com.parusya.infra.security.dto.AuthDtos.UserSummary;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login/organizer")
    public ResponseEntity<LoginResponse> loginOrganizer(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticate(request, UserRole.ORGANIZER));
    }

    @PostMapping("/login/staff")
    public ResponseEntity<LoginResponse> loginStaff(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticate(request, UserRole.EVENT_STAFF));
    }

    @PostMapping("/login/participant")
    public ResponseEntity<LoginResponse> loginParticipant(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticate(request, UserRole.PARTICIPANT));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private LoginResponse authenticate(LoginRequest request, UserRole role) {
        // O username é composto para o CustomUserDetailsService saber em qual
        // repositório buscar: "email:ROLE"
        String compositeUsername = request.email() + ":" + role.name();

        try {
            var authToken = new UsernamePasswordAuthenticationToken(
                    compositeUsername, request.password()
            );
            var authentication = authenticationManager.authenticate(authToken);
            var user = (AuthenticatedUser) authentication.getPrincipal();

            String token = jwtService.generateToken(user);

            return new LoginResponse(
                    token,
                    jwtService.getExpirationSeconds(),
                    new UserSummary(
                            user.getId().toString(),
                            user.getName(),
                            user.getUsername(),
                            user.getRole().name()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new BusinessException(BusinessException.ErrorCode.INVALID_CREDENTIALS);
        }
    }
}