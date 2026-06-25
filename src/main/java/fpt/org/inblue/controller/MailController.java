package fpt.org.inblue.controller;


import lombok.RequiredArgsConstructor;
import fpt.org.inblue.service.MailService;
import jakarta.mail.MessagingException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mails")
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;

    @GetMapping("/send")
    public void adminSendMail(@RequestParam String toEmail,
                              @RequestParam String subject,
                              @RequestParam String body) throws MessagingException {
            mailService.adminSendMail(toEmail, subject, body);
    }

}
