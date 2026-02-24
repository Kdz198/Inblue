package fpt.org.inblue.service;

import jakarta.mail.MessagingException;

public interface MailService {
    void adminSendMail(String toEmail, String subject, String body) throws MessagingException;

}
