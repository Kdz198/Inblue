package fpt.org.inblue.model.dto;

import fpt.org.inblue.model.Mentor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MentorEventDto {
    Mentor mentor;
    MultipartFile file ;
    String message;


    public MentorEventDto(Mentor mentor, MultipartFile file, String message) {
        this.mentor = mentor;
        this.file = file;
        this.message=message;
    }

}
