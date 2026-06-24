package fpt.org.inblue.service.impl;

import fpt.org.inblue.model.User;
import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.service.MailService;
import fpt.org.inblue.service.PasswordResetService;
import fpt.org.inblue.exception.CustomException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

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
            throw new CustomException("Email không tồn tại trong hệ thống", HttpStatus.NOT_FOUND);
        }

        String otp = String.format("%06d", new Random().nextInt(1000000));

        String redisKey = OTP_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(redisKey, otp, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);

        String subject = "[Inblue] Mã OTP xác nhận đặt lại mật khẩu";
        String body = "<div style='font-family: Arial, sans-serif; padding: 20px;'>" +
                "<h2>Yêu cầu đặt lại mật khẩu</h2>" +
                "<p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn trên hệ thống Inblue.</p>" +
                "<p>Mã OTP của bạn là: <strong style='font-size: 20px; color: #1a73e8; letter-spacing: 2px;'>" + otp + "</strong></p>" +
                "<p>Mã này có hiệu lực trong vòng <strong>" + OTP_EXPIRY_MINUTES + " phút</strong>. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>" +
                "<br/>" +
                "<p>Trân trọng,<br/>Đội ngũ Inblue</p>" +
                "</div>";

        try {
            mailService.adminSendMail(email, subject, body);
            log.info("Đã gửi mã OTP đặt lại mật khẩu thành công đến: {}", email);
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email đặt lại mật khẩu: {}", e.getMessage());
            throw new CustomException("Gửi email thất bại, vui lòng thử lại sau.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        String redisKey = OTP_KEY_PREFIX + email;
        String savedOtp = (String) redisTemplate.opsForValue().get(redisKey);

        if (savedOtp == null) {
            throw new CustomException("Mã OTP đã hết hạn hoặc không tồn tại. Vui lòng yêu cầu mã mới.", HttpStatus.BAD_REQUEST);
        }

        if (!savedOtp.equals(otp)) {
            throw new CustomException("Mã OTP không hợp lệ.", HttpStatus.BAD_REQUEST);
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
                throw new CustomException("Không tìm thấy người dùng sau khi xác thực OTP.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        redisTemplate.delete(redisKey);
        log.info("Đặt lại mật khẩu thành công cho email: {}", email);
    }
}
