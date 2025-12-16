package fpt.org.inblue.event;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.MentorEventDto;
import fpt.org.inblue.model.dto.UserEventDto;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
public class EventListenerHandle {


    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final MentorRepository mentorRepository;

    public EventListenerHandle(UserRepository userRepository, CloudinaryService cloudinaryService, MentorRepository mentorRepository) {
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
        this.mentorRepository = mentorRepository;
    }

    @Async
    @EventListener(condition = "#userEventDto.message == 'cv'")
    public void handleCv(UserEventDto userEventDto) {
        System.out.println("Handling CV upload event for user ID: " + userEventDto.getUser().getId());
        User user = userRepository.findById(userEventDto.getUser().getId()).orElse(null);
        if (user != null) {
                try {
                    uploadPdf(user, userEventDto.getFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
            }
        }
    }

    @Async
    @EventListener(condition = "#userEventDto.message == 'avatar'")
    public void handleAvatar(UserEventDto userEventDto) {
        System.out.println("Handling avatar upload event for user ID: " + userEventDto.getUser().getId());
        try {
            Thread.sleep(6000); //chờ 5s trc khi get user lên để tránh việc bị đọc dữ liệu cũ do cv chưa save kịp
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        User user = userRepository.findById(userEventDto.getUser().getId()).orElse(null);
        if (user != null) {
            try {
                uploadImg(user, userEventDto.getFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @EventListener(condition = "#mentorEventDto.message == 'avatar'")
    @Async
    public void handleMentorUploadAvatar(MentorEventDto mentorEventDto) {
        System.out.println("Handling avatar upload event for mentor ID: " + mentorEventDto.getMentor().getId());
        Mentor mentor = mentorRepository.findById(mentorEventDto.getMentor().getId()).orElse(null);
        if (mentor != null) {
            try {
                uploadImgMentor(mentor, mentorEventDto.getFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Async
    @EventListener(condition = "#mentorEventDto.message =='IdentityCard'")
    public void handleMentorUploadIdentity(MentorEventDto mentorEventDto) {
        System.out.println("Handling identity card upload event for mentor ID: " + mentorEventDto.getMentor().getId());
        try {
            Thread.sleep(6000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        Mentor mentor = mentorRepository.findById(mentorEventDto.getMentor().getId()).orElse(null);
        if(mentor!=null){
            try {
                uploadCertificate(mentor, mentorEventDto.getFile(),"IdentityCard");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Async
    @EventListener(condition = "#mentorEventDto.message =='Degree'")
    public void handleMentorUploadDegree(MentorEventDto mentorEventDto) {
        System.out.println("Handling degree upload event for mentor ID: " + mentorEventDto.getMentor().getId());
        try {
            Thread.sleep(12000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        Mentor mentor = mentorRepository.findById(mentorEventDto.getMentor().getId()).orElse(null);
        if(mentor!=null){
            try {
                uploadCertificate(mentor, mentorEventDto.getFile(),"Degree");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Async
    @EventListener(condition = "#mentorEventDto.message =='Other'")
    public void handleMentorUploadOther(MentorEventDto mentorEventDto) {
        System.out.println("Handling other file upload event for mentor ID: " + mentorEventDto.getMentor().getId());
        try {
            Thread.sleep(18000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        Mentor mentor = mentorRepository.findById(mentorEventDto.getMentor().getId()).orElse(null);
        if(mentor!=null){
            try {
                uploadCertificate(mentor, mentorEventDto.getFile(),"Other");
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

    public void uploadImg(User user, MultipartFile file) throws IOException{
        Map<String,String> map = cloudinaryService.uploadImg(file);
        user.setAvatarUrl(map.get("secure_url"));
        user.setPublic_id(map.get("public_id"));
        userRepository.save(user);
    }

    public void uploadImgMentor(Mentor mentor, MultipartFile file) throws IOException {
        Map<String,String> map = cloudinaryService.uploadImg(file);
        mentor.setAvatarUrl(map.get("secure_url"));
        mentor.setPublic_id(map.get("public_id"));
        mentorRepository.save(mentor);
    }

    public void uploadCertificate(Mentor mentor, MultipartFile file,String certificateName) throws IOException {
        Map<String,String> map = cloudinaryService.uploadDocument(file);
        String certificateUrl = map.get("secure_url");
        String certificatePublicId = map.get("public_id");
        if(certificateName.equals("IdentityCard")){
            mentor.setIdentityImg(certificateUrl);
            mentor.setPublic_id_identity(certificatePublicId);
            mentorRepository.save(mentor);
        }
        else if(certificateName.equals("Degree")){
            mentor.setDegreeImg(certificateUrl);
            mentor.setPublic_id_degree(certificatePublicId);
            mentorRepository.save(mentor);
        }
        else if(certificateName.equals("Other")){
            mentor.setOtherFile(certificateUrl);
            mentor.setPublic_id_other(certificatePublicId);
            mentorRepository.save(mentor);
        }

    }
}
