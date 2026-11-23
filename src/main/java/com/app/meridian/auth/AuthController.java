package com.app.meridian.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.meridian.auth.dto.ForgotPasswordRequestDTO;
import com.app.meridian.auth.dto.ForgotPasswordResponseDTO;
import com.app.meridian.auth.dto.LoginRequestDTO;
import com.app.meridian.auth.dto.LoginResponseDTO;
import com.app.meridian.auth.dto.LogoutRequestDTO;
import com.app.meridian.auth.dto.RefreshTokenRequestDTO;
import com.app.meridian.auth.dto.ResetPasswordRequestDTO;
import com.app.meridian.auth.dto.TokenResponseDTO;
import com.app.meridian.auth.services.IssuedTokens;
import com.app.meridian.auth.services.LoginService;
import com.app.meridian.auth.services.LogoutService;
import com.app.meridian.auth.services.PasswordResetService;
import com.app.meridian.auth.services.TokenIssuer;
import com.app.meridian.user.dto.UserResponseDTO;
import com.app.meridian.user.services.GetCurrentUser;
import com.app.meridian.user.utils.UserMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/auth")
public class AuthController {

    private final LoginService loginService;
    private final TokenIssuer tokenIssuer;
    private final LogoutService logoutService;
    private final GetCurrentUser getCurrentUser;
    private final PasswordResetService passwordResetService;
    private final UserMapper userMapper;

    /** Returns an access token (30 min by default) plus a refresh token (14 days by default). */
    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {
        return loginService.login(request);
    }

    /** Exchanges a refresh token for a new pair. The refresh token presented is spent in the process. */
    @PostMapping("/refresh")
    public TokenResponseDTO refresh(@RequestBody RefreshTokenRequestDTO request) {
        IssuedTokens tokens = tokenIssuer.rotate(request == null ? null : request.refreshToken());
        return new TokenResponseDTO(tokens.accessToken(), tokens.refreshToken(), tokens.tokenType(),
                tokens.expiresInMinutes(), tokens.refreshExpiresInMinutes());
    }

    /** Revokes the refresh token in the body, or every session of the account when allDevices is true. */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody(required = false) LogoutRequestDTO request) {
        logoutService.logout(request);
    }

    @GetMapping("/me")
    public UserResponseDTO me() {
        return userMapper.toResponse(getCurrentUser.get());
    }

    /**
     * Always answers 202 with the same message. The token is mailed; it is also returned in the body
     * while {@code meridian.auth.expose-reset-token} is on, which is how an API-only setup (and
     * the smoke test) can complete the flow without a mail server.
     */
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ForgotPasswordResponseDTO forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        return passwordResetService.request(request == null ? null : request.email());
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        passwordResetService.reset(request.token(), request.newPassword());
    }
}
