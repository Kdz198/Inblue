package fpt.org.inblue.service;

import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.dto.MentorInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MentorService {
     Mentor createMentor(MentorInfo data, MultipartFile identityFile, MultipartFile degreeFile, MultipartFile otherFile,MultipartFile avatar) throws IOException;
     Mentor getMentorById(int id);
     List<Mentor> getAllMentors();
     Mentor updateMentor( Mentor mentor);
     void toggleActive(int id);

}
