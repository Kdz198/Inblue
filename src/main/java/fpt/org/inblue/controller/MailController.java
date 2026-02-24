package fpt.org.inblue.controller;

import fpt.org.inblue.service.MailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mails")
public class MailController {
    @Autowired
    private MailService mailService;

    @GetMapping("/send")
    public void adminSendMail(@RequestParam String toEmail,
                              @RequestParam String subject,
                              @RequestParam String body) throws MessagingException {
            mailService.adminSendMail(toEmail, subject, body);
    }

}
