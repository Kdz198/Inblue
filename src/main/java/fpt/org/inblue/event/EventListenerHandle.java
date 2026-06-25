package fpt.org.inblue.event;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.MentorEventDto;
import fpt.org.inblue.model.dto.UserEventDto;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.security.JwtUtils;
import java.io.IOException;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class EventListenerHandle {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final MentorRepository mentorRepository;
    private final JwtUtils jwtUtils;

    public EventListenerHandle(
            UserRepository userRepository,
            CloudinaryService cloudinaryService,
            MentorRepository mentorRepository,
            JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
        this.mentorRepository = mentorRepository;
        this.jwtUtils = jwtUtils;
    }

    @Async
    @EventListener(condition = "#userEventDto.message == 'cv'")
    public void handleCv(UserEventDto userEventDto) {
        User user = userRepository.findById(userEventDto.getUser().getId()).orElse(null);
        if (user != null) {
            try {
                if (user.getCv_public_id() != null) {
                    cloudinaryService.deletePdf(user.getCv_public_id());
                }
                uploadPdf(user, userEventDto.getFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Async
    @EventListener(condition = "#userEventDto.message == 'avatar'")
    public void handleAvatar(UserEventDto userEventDto) {

        User user = userRepository.findById(userEventDto.getUser().getId()).orElse(null);
        if (user != null) {
            try {
                uploadImg(user, userEventDto.getFile());
                System.out.println("Avatar uploaded successfully");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @EventListener(condition = "#mentorEventDto.message == 'avatar'")
    @Async
    public void handleMentorUploadAvatar(MentorEventDto mentorEventDto) {
        Mentor mentor =
                mentorRepository.findById(mentorEventDto.getMentor().getId()).orElse(null);
        if (mentor != null) {
            try {
                uploadImgMentor(mentor, mentorEventDto.getFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void uploadPdf(User user, MultipartFile file) throws IOException {
        Map<String, String> map = cloudinaryService.uploadDocument(file);
        user.setCvUrl(map.get("secure_url"));
        user.setCv_public_id(map.get("public_id"));
        userRepository.save(user);
    }

    public void uploadImg(User user, MultipartFile file) throws IOException {
        Map<String, String> map = cloudinaryService.uploadImg(file);
        user.setAvatarUrl(map.get("secure_url"));
        user.setPublic_id(map.get("public_id"));
        userRepository.save(user);
    }

    public void uploadImgMentor(Mentor mentor, MultipartFile file) throws IOException {
        Map<String, String> map = cloudinaryService.uploadImg(file);
        mentorRepository.updateAvatar(mentor.getId(), map.get("secure_url"), map.get("public_id"));
    }
}
