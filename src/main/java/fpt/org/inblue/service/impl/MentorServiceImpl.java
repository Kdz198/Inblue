package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.dto.CreateMentorRequest;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.service.MentorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MentorServiceImpl implements MentorService {
    @Autowired
    private MentorRepository mentorRepository;

    @Override
    public Mentor createMentor(CreateMentorRequest mentor) {
        Mentor mentorEntity = new Mentor();
        mentorEntity.setName(mentor.getName());
        mentorEntity.setEmail(mentor.getEmail());
        mentorEntity.setPassword(mentor.getPassword());
        mentorEntity.setRole(mentor.getRole());
        mentorEntity.setActive(mentor.isActive());
        mentorEntity.setBio(mentor.getBio());
        mentorEntity.setAvatarUrl(mentor.getAvatarUrl());
        mentorEntity.setExpertise(mentor.getExpertise());
        mentorEntity.setYearsOfExperience(mentor.getYearsOfExperience());
        mentorEntity.setLinkedInUrl(mentor.getLinkedInUrl());
        mentorEntity.setCurrentCompany(mentor.getCurrentCompany());
        mentorEntity.setRate(mentor.getRate());
        mentorEntity.setCertificateUrl(mentor.getCertificateUrl());
        mentorEntity.setTotalSession(mentor.getTotalSession());
        return mentorRepository.save(mentorEntity);
    }

    @Override
    public Mentor getMentorById(int id) {
        if(mentorRepository.existsById(id)){
            return mentorRepository.findById(id).get();
        }
        else {
            throw new CustomException("Mentor not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<Mentor> getAllMentors() {
        return mentorRepository.findAll();
    }

    @Override
    public Mentor updateMentor(Mentor mentor) {
        if(mentorRepository.existsById(mentor.getId())){
            return mentorRepository.save(mentor);
        }
        else {
            throw new CustomException("Mentor not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void toggleActive(int id) {
        if(mentorRepository.existsById(id)){
            Mentor mentor = mentorRepository.findById(id).get();
            if(mentor.isActive()){
                mentor.setActive(false);
                mentorRepository.save(mentor);
            }
            else{
                mentor.setActive(true);
                mentorRepository.save(mentor);
            }
        }
        else {
            throw new CustomException("Mentor not found", HttpStatus.NOT_FOUND);
        }
    }
}
