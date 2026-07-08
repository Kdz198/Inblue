package fpt.org.inblue.controller;

import fpt.org.inblue.model.dto.request.GenericEmailRequest;
import fpt.org.inblue.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mails")
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;

    @Operation(
            summary = "Gửi email quản trị",
            description =
                    "Gửi email từ tài khoản quản trị hệ thống đến một địa chỉ nhận nhất định (phương thức GET, ném ngoại lệ nếu lỗi).")
    @GetMapping("/send")
    public void adminSendMail(@RequestParam String toEmail, @RequestParam String subject, @RequestParam String body)
            throws MessagingException {
        mailService.adminSendMail(toEmail, subject, body);
    }

    @Operation(
            summary = "Gửi email chung (Generic)",
            description =
                    "API gửi email dùng chung, nhận payload chứa địa chỉ nhận, tiêu đề và nội dung email. Lỗi được xử lý nội bộ và trả về HTTP status tương ứng.")
    @PostMapping("/send-generic")
    public void sendGenericEmail(@RequestBody GenericEmailRequest request) {
        mailService.sendEmail(request.getToEmail(), request.getSubject(), request.getBody());
    }
}
