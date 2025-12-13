package fpt.org.inblue.event;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.UserCvDtoRequest;
import fpt.org.inblue.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
public class EventListenerHandle {


    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public EventListenerHandle(UserRepository userRepository, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @EventListener
    public void handleCreateUser(UserCvDtoRequest userCvDtoRequest) {
        User user = userRepository.findById(userCvDtoRequest.getUser().getId()).orElse(null);
        if (user != null) {
            if (userCvDtoRequest.getMessage().equals("cv")) {
                try {
                    uploadPdf(user, userCvDtoRequest.getFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (userCvDtoRequest.getMessage().equals("avatar")) {
                try {
                    uploadImg(user, userCvDtoRequest.getFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void uploadPdf(User user, MultipartFile file) throws IOException {
        Map<String, String> map = cloudinaryService.uploadDocument(file);
        user.setCvUrl(map.get("secure_url"));
        user.setCv_public_id(map.get("public_id"));
        userRepository.save(user);
    }

    public void uploadImg(User user, MultipartFile file) throws IOException{
        Map<String,String> map = cloudinaryService.uploadImg(file);
        user.setAvatarUrl(map.get("secure_url"));
        user.setPublic_id(map.get("public_id"));
        userRepository.save(user);
    }

}
