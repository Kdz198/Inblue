package fpt.org.inblue.service;

import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.dto.CreateMentorRequest;
import fpt.org.inblue.model.dto.MentorInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MentorService {
    public Mentor createMentor(MentorInfo data, MultipartFile identityFile, MultipartFile degreeFile, MultipartFile otherFile,MultipartFile avatar) throws IOException;
    public Mentor getMentorById(int id);
    public List<Mentor> getAllMentors();
    public Mentor updateMentor( Mentor mentor);
    public void toggleActive(int id);
}
