package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.User;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.MailService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceActiveFlowTest {
    @Mock
    UserRepository userRepository;

    @Mock
    MentorRepository mentorRepository;

    @Mock
    MailService mailService;

    @Mock
    RedisTemplate<String, Object> redisTemplate;

    @Mock
    ValueOperations<String, Object> values;

    @Mock
    PasswordEncoder passwordEncoder;

    private PasswordResetServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetServiceImpl(
                userRepository, mentorRepository, mailService, redisTemplate, passwordEncoder);
        lenient().when(redisTemplate.opsForValue()).thenReturn(values);
    }

    @Test
    void sendResetOtpRejectsUnknownEmail() {
        when(userRepository.existsByEmail("missing@example.com")).thenReturn(false);
        when(mentorRepository.findByEmail("missing@example.com")).thenReturn(null);
        CustomException error = assertThrows(CustomException.class, () -> service.sendResetOtp("missing@example.com"));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    void sendResetOtpStoresShortLivedOtpForExistingUser() throws Exception {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);
        when(mentorRepository.findByEmail("user@example.com")).thenReturn(null);
        service.sendResetOtp("user@example.com");
        verify(values)
                .set(
                        any(String.class),
                        any(String.class),
                        org.mockito.ArgumentMatchers.eq(5L),
                        org.mockito.ArgumentMatchers.eq(TimeUnit.MINUTES));
        verify(mailService).adminSendMail(any(String.class), any(String.class), any(String.class));
    }

    @Test
    void resetPasswordRejectsBlankNewPasswordBeforeReadingOtp() {
        CustomException error =
                assertThrows(CustomException.class, () -> service.resetPassword("user@example.com", "123456", "  "));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    void resetPasswordRejectsMissingOtp() {
        when(values.get("reset_password_otp:user@example.com")).thenReturn(null);
        CustomException error = assertThrows(
                CustomException.class, () -> service.resetPassword("user@example.com", "123456", "new-pass"));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    void resetPasswordRejectsWrongOtp() {
        when(values.get("reset_password_otp:user@example.com")).thenReturn("654321");
        CustomException error = assertThrows(
                CustomException.class, () -> service.resetPassword("user@example.com", "123456", "new-pass"));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    void resetPasswordEncodesAndDeletesOtpForUser() {
        User user = new User();
        user.setPassword("old");
        when(values.get("reset_password_otp:user@example.com")).thenReturn("123456");
        when(userRepository.findByEmail("user@example.com")).thenReturn(user);
        when(passwordEncoder.encode("new-pass")).thenReturn("encoded");
        service.resetPassword("user@example.com", "123456", "new-pass");
        assertEquals("encoded", user.getPassword());
        verify(userRepository).save(user);
        verify(redisTemplate).delete("reset_password_otp:user@example.com");
    }
}
