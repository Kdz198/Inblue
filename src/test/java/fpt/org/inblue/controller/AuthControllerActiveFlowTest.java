package fpt.org.inblue.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.model.dto.request.ForgotPasswordRequest;
import fpt.org.inblue.model.dto.request.LoginRequest;
import fpt.org.inblue.model.dto.request.ResetPasswordRequest;
import fpt.org.inblue.security.JwtUtils;
import fpt.org.inblue.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthControllerActiveFlowTest {
    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    JwtUtils jwtUtils;

    @Mock
    PasswordResetService passwordResetService;

    @Mock
    Authentication authentication;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authenticationManager, jwtUtils, passwordResetService);
    }

    @Test
    void loginAuthenticatesAndReturnsBearerToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("secret");
        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any()))
                .thenReturn(authentication);
        when(jwtUtils.generateToken(authentication)).thenReturn("token");
        var response = controller.login(request);
        assertEquals("token", response.getBody());
        assertEquals("Bearer token", response.getHeaders().getFirst("Authorization"));
    }

    @Test
    void googleLoginRedirectsToOauthAuthorization() {
        assertEquals("/oauth2/authorization/google", controller.googleLogin().getUrl());
    }

    @Test
    void forgotPasswordDelegatesEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("user@example.com");
        assertEquals(200, controller.forgotPassword(request).getStatusCode().value());
        verify(passwordResetService).sendResetOtp("user@example.com");
    }

    @Test
    void resetPasswordDelegatesCompletePayload() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("user@example.com");
        request.setOtp("123456");
        request.setNewPassword("new-password");
        assertEquals(200, controller.resetPassword(request).getStatusCode().value());
        verify(passwordResetService).resetPassword("user@example.com", "123456", "new-password");
    }
}
