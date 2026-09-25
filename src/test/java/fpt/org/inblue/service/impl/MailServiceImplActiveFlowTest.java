package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.exception.CustomException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

class MailServiceImplActiveFlowTest {
    @Test
    void adminSendMailBuildsAndSendsHtmlMessage() throws Exception {
        JavaMailSender sender = org.mockito.Mockito.mock(JavaMailSender.class);
        MimeMessage message = new jakarta.mail.internet.MimeMessage((jakarta.mail.Session) null);
        when(sender.createMimeMessage()).thenReturn(message);
        MailServiceImpl service = new MailServiceImpl(sender);
        ReflectionTestUtils.setField(service, "fromEmail", "noreply@inblue.test");

        service.adminSendMail("user@example.com", "Subject", "<b>Body</b>");

        verify(sender).send(message);
        assertEquals("Subject", message.getSubject());
    }

    @Test
    void sendEmailWrapsMailFailure() throws Exception {
        MailServiceImpl service = spy(new MailServiceImpl(org.mockito.Mockito.mock(JavaMailSender.class)));
        doThrow(new jakarta.mail.MessagingException("offline"))
                .when(service)
                .adminSendMail("user@example.com", "Subject", "Body");
        doCallRealMethod().when(service).sendEmail("user@example.com", "Subject", "Body");

        CustomException error =
                assertThrows(CustomException.class, () -> service.sendEmail("user@example.com", "Subject", "Body"));
        assertEquals(500, error.getStatus().value());
    }
}
