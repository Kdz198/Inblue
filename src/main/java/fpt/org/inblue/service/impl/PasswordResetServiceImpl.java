package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.User;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.MailService;
import fpt.org.inblue.service.PasswordResetService;
import jakarta.mail.MessagingException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final MentorRepository mentorRepository;
    private final MailService mailService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    private static final String OTP_KEY_PREFIX = "reset_password_otp:";
    private static final long OTP_EXPIRY_MINUTES = 5;

    @Override
    public void sendResetOtp(String email) {
        boolean isUser = userRepository.existsByEmail(email);
        boolean isMentor = mentorRepository.findByEmail(email) != null;

        if (!isUser && !isMentor) {
            throw new CustomException("Email does not exist in the system", HttpStatus.NOT_FOUND);
        }

        String otp = String.format("%06d", new Random().nextInt(1000000));

        String redisKey = OTP_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(redisKey, otp, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);

        String subject = "[Inblue] OTP Code to Reset Your Password";
        String body = "<div style='font-family: Arial, sans-serif; padding: 20px;'>" + "<h2>Reset Password Request</h2>"
                + "<p>We received a request to reset the password for your account on the Inblue system.</p>"
                + "<p>Your OTP code is: <strong style='font-size: 20px; color: #1a73e8; letter-spacing: 2px;'>"
                + otp + "</strong></p>" + "<p>This code is valid for <strong>"
                + OTP_EXPIRY_MINUTES + " minutes</strong>. Please do not share this code with anyone.</p>" + "<br/>"
                + "<p>Best regards,<br/>The Inblue Team</p>"
                + "</div>";

        try {
            mailService.adminSendMail(email, subject, body);
            log.info("Sent password reset OTP successfully to: {}", email);
        } catch (MessagingException e) {
            log.error("Error sending password reset email: {}", e.getMessage());
            throw new CustomException(
                    "Failed to send email, please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        String redisKey = OTP_KEY_PREFIX + email;
        String savedOtp = (String) redisTemplate.opsForValue().get(redisKey);

        if (savedOtp == null) {
            throw new CustomException(
                    "OTP code has expired or does not exist. Please request a new one.", HttpStatus.BAD_REQUEST);
        }

        if (!savedOtp.equals(otp)) {
            throw new CustomException("Invalid OTP code.", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } else {
            Mentor mentor = mentorRepository.findByEmail(email);
            if (mentor != null) {
                mentor.setPassword(passwordEncoder.encode(newPassword));
                mentorRepository.save(mentor);
            } else {
                throw new CustomException("User not found after OTP verification.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        redisTemplate.delete(redisKey);
        log.info("Successfully reset password for email: {}", email);
    }
}
