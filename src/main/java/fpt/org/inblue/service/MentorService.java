package fpt.org.inblue.service;

import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.dto.MentorInfo;
import fpt.org.inblue.model.dto.response.MentorResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MentorService {
     Mentor createMentor(MentorInfo data, MultipartFile identityFile, MultipartFile degreeFile, MultipartFile otherFile,MultipartFile avatar) throws IOException;
     MentorResponse getMentorById(int id);
     List<MentorResponse> getAllMentors();
     void toggleActive(int id);

}
