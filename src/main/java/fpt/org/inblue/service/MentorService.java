package fpt.org.inblue.service;

import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.dto.CreateMentorRequest;

import java.util.List;

public interface MentorService {
    public Mentor createMentor(CreateMentorRequest mentor);
    public Mentor getMentorById(int id);
    public List<Mentor> getAllMentors();
    public Mentor updateMentor( Mentor mentor);
    public void toggleActive(int id);
}
