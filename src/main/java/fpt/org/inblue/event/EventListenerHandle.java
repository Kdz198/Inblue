package fpt.org.inblue.event;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.MentorCvDto;
import fpt.org.inblue.model.dto.UserCvDtoRequest;
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
    @EventListener(condition = "#userCvDtoRequest.message == 'cv'")
    public void handleCv(UserCvDtoRequest userCvDtoRequest) {
        System.out.println("Handling CV upload event for user ID: " + userCvDtoRequest.getUser().getId());
        User user = userRepository.findById(userCvDtoRequest.getUser().getId()).orElse(null);
        if (user != null) {
                try {
                    uploadPdf(user, userCvDtoRequest.getFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
            }
        }
    }

    @Async
    @EventListener(condition = "#userCvDtoRequest.message == 'avatar'")
    public void handleAvatar(UserCvDtoRequest userCvDtoRequest) {
        System.out.println("Handling avatar upload event for user ID: " + userCvDtoRequest.getUser().getId());
        try {
            Thread.sleep(5000); //chờ 5s trc khi get user lên để tránh việc bị đọc dữ liệu cũ do cv chưa save kịp
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        User user = userRepository.findById(userCvDtoRequest.getUser().getId()).orElse(null);
        if (user != null) {
            try {
                uploadImg(user, userCvDtoRequest.getFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @EventListener(condition = "#mentorCvDto.message == 'avatar'")
    @Async
    public void handleMentorUploadAvatar(MentorCvDto mentorCvDto) {
        System.out.println("Handling avatar upload event for mentor ID: " + mentorCvDto.getMentor().getId());
        Mentor mentor = mentorRepository.findById(mentorCvDto.getMentor().getId()).orElse(null);
        if (mentor != null) {
            try {
                uploadImgMentor(mentor, mentorCvDto.getFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Async
    @EventListener(condition = "#mentorCvDto.message =='IdentityCard'")
    public void handleMentorUploadIdentity(MentorCvDto mentorCvDto) {
        System.out.println("Handling identity card upload event for mentor ID: " + mentorCvDto.getMentor().getId());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        Mentor mentor = mentorRepository.findById(mentorCvDto.getMentor().getId()).orElse(null);
        if(mentor!=null){
            try {
                uploadCertificate(mentor, mentorCvDto.getFile(),"IdentityCard");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Async
    @EventListener(condition = "#mentorCvDto.message =='Degree'")
    public void handleMentorUploadDegree(MentorCvDto mentorCvDto) {
        System.out.println("Handling degree upload event for mentor ID: " + mentorCvDto.getMentor().getId());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        Mentor mentor = mentorRepository.findById(mentorCvDto.getMentor().getId()).orElse(null);
        if(mentor!=null){
            try {
                uploadCertificate(mentor, mentorCvDto.getFile(),"Degree");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Async
    @EventListener(condition = "#mentorCvDto.message =='Other'")
    public void handleMentorUploadOther(MentorCvDto mentorCvDto) {
        System.out.println("Handling other file upload event for mentor ID: " + mentorCvDto.getMentor().getId());
        try {
            Thread.sleep(15000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        Mentor mentor = mentorRepository.findById(mentorCvDto.getMentor().getId()).orElse(null);
        if(mentor!=null){
            try {
                uploadCertificate(mentor, mentorCvDto.getFile(),"Other");
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
