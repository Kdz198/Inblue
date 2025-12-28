package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.dto.MentorInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMentorRequest {
MentorInfo data;
    MultipartFile avatar;
     MultipartFile identityFile;
    MultipartFile degreeFile;
     MultipartFile otherFile;
}
