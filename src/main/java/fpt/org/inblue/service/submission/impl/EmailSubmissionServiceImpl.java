package fpt.org.inblue.service.submission.impl;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.model.EmailSubmission;
import fpt.org.inblue.repository.EmailSubmissionRepository;
import fpt.org.inblue.service.submission.EmailSubmissionService;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.FlagTerm;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSubmissionServiceImpl implements EmailSubmissionService {

    private final EmailSubmissionRepository emailSubmissionRepository;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Override
    public Optional<EmailSubmission> getById(Long id) {
        return emailSubmissionRepository.findById(id);
    }

    @Override
    public List<EmailSubmission> getAll() {
        return emailSubmissionRepository.findAll();
    }

    @Override
    public void fetchEmails() {
        if (host == null || host.isEmpty() || username == null || username.isEmpty()) {
            log.warn("IMAP config is missing, skipping email fetch.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", host);
        props.put("mail.imaps.port", "993");

        try {
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect(host, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Fetch unseen messages
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            // Lấy tối đa 20 mail mỗi đợt chạy để tránh timeout / FolderClosedException
            int limit = Math.min(messages.length, 20);
            log.info("Found {} unseen emails, processing top {}", messages.length, limit);

            Pattern subjectPattern = Pattern.compile("\\[INBLUE-APP-(\\d+)\\]");

            for (int i = 0; i < limit; i++) {
                Message message = messages[i];
                try {
                    // Check if folder was closed by server during long processing, reopen if needed
                    if (!inbox.isOpen()) {
                        inbox.open(Folder.READ_WRITE);
                    }
                    String subject = message.getSubject() != null ? message.getSubject() : "";
                    String sender = message.getFrom()[0].toString();
                    LocalDateTime receivedAt = message.getReceivedDate() != null
                            ? message.getReceivedDate()
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime()
                            : LocalDateTime.now();

                    Matcher matcher = subjectPattern.matcher(subject);
                    Long applicationId = null;
                    if (matcher.find()) {
                        applicationId = Long.parseLong(matcher.group(1));
                    }

                    StringBuilder bodyText = new StringBuilder();
                    List<String> attachmentUrls = new ArrayList<>();

                    extractContent(message, bodyText, attachmentUrls);

                    EmailSubmission submission = EmailSubmission.builder()
                            .applicationId(applicationId)
                            .senderEmail(sender)
                            .subject(subject)
                            .bodyText(bodyText.toString().trim())
                            .status(
                                    applicationId != null
                                            ? EmailSubmission.EmailStatus.PENDING
                                            : EmailSubmission.EmailStatus.IGNORED)
                            .errorMessage(applicationId == null ? "Missing or invalid application ID in subject" : null)
                            .attachmentUrls(objectMapper.writeValueAsString(attachmentUrls))
                            .receivedAt(receivedAt)
                            .build();

                    emailSubmissionRepository.save(submission);

                    // Mark as SEEN
                    message.setFlag(Flags.Flag.SEEN, true);
                    log.info("Processed email from {} with subject {}", sender, subject);

                } catch (Exception e) {
                    log.error("Error processing an email", e);
                }
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            log.error("Failed to fetch emails via IMAP", e);
        }
    }

    private void extractContent(Part part, StringBuilder bodyText, List<String> attachmentUrls) throws Exception {
        if (part.isMimeType("text/plain")) {
            bodyText.append(part.getContent().toString()).append("\n");
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                extractContent(multipart.getBodyPart(i), bodyText, attachmentUrls);
            }
        } else if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || part.getFileName() != null) {
            try (InputStream is = part.getInputStream()) {
                byte[] bytes = is.readAllBytes();
                String fileName = part.getFileName() != null ? part.getFileName() : "attachment";
                Map<String, String> uploadResult = cloudinaryService.uploadDocument(bytes, fileName);
                if (uploadResult != null && uploadResult.containsKey("secure_url")) {
                    attachmentUrls.add(uploadResult.get("secure_url"));
                }
            }
        }
    }
}
