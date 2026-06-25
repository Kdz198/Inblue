package fpt.org.inblue.service.impl;


import lombok.RequiredArgsConstructor;
import fpt.org.inblue.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void adminSendMail(String toEmail, String subject, String body) throws MessagingException {
        MimeMessage mimemessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimemessage,true,"UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body,true);
        mailSender.send(mimemessage);
    }
}
