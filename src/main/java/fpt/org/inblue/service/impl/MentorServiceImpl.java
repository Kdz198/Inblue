package fpt.org.inblue.service.impl;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.MentorMapper;
import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.dto.request.CreateMentorRequest;
import fpt.org.inblue.model.dto.MentorEventDto;
import fpt.org.inblue.model.dto.MentorInfo;
import fpt.org.inblue.model.dto.response.MentorResponse;
import fpt.org.inblue.model.enums.Role;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.service.MentorService;
import fpt.org.inblue.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class MentorServiceImpl implements MentorService {
    @Autowired
    private MentorRepository mentorRepository;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private MentorMapper mentorMapper;

    @Override
    public Mentor createMentor(MentorInfo data, MultipartFile identityFile, MultipartFile degreeFile, MultipartFile otherFile,MultipartFile avatar) throws IOException {
        Mentor mentor;
       if(data.getId()==null){
           mentor = mentorMapper.toEntity(data);
           mentor.setRole(Role.MENTOR);
           mentor.setActive(false);
           mentor.setTotalSession(0);
           mentor.setAverageRating(0);
           mentor = mentorRepository.save(mentor);
           processAndPublishFileEvent(mentor, avatar, "avatar");
           processAndPublishFileEvent(mentor, identityFile, "IdentityCard");
           processAndPublishFileEvent(mentor, degreeFile, "Degree");
           processAndPublishFileEvent(mentor, otherFile, "Other");
           return mentor;
       }
       else{
           mentor = mentorRepository.findById(data.getId()).orElseThrow(() -> new RuntimeException("Mentor Not Found"));
           mentorMapper.updateMentorFromDto(data, mentor);

            if(mentor.getAvatarUrl()!=null) {
                mentor.setAvatarUrl(mentor.getAvatarUrl());
                mentor.setPublic_id(mentor.getPublic_id());
            }
            if(mentor.getIdentityImg()!=null) {
                mentor.setIdentityImg(mentor.getIdentityImg());
                mentor.setPublic_id_identity(mentor.getPublic_id_identity());
            }
            if(mentor.getDegreeImg()!=null) {
                mentor.setDegreeImg(mentor.getDegreeImg());
                mentor.setPublic_id_degree(mentor.getPublic_id_degree());
            }
            if(mentor.getOtherFile()!=null) {
                mentor.setOtherFile(mentor.getOtherFile());
                mentor.setPublic_id_other(mentor.getPublic_id_other());
            }
           mentor = mentorRepository.save(mentor);
           if (avatar!=null &&!avatar.isEmpty()) {
               if(mentor.getPublic_id()!=null) {
                   cloudinaryService.deleteImage(mentor.getPublic_id());
               }
               processAndPublishFileEvent(mentor, avatar, "avatar");
           }
           if (identityFile!=null && !identityFile.isEmpty()) {
               if(mentor.getPublic_id_identity()!=null) {
               cloudinaryService.deletePdf(mentor.getPublic_id_identity());}
               processAndPublishFileEvent(mentor, identityFile, "IdentityCard");
           }
           if (degreeFile!=null && !degreeFile.isEmpty()) {
               if(mentor.getPublic_id_degree()!=null){
               cloudinaryService.deletePdf(mentor.getPublic_id_degree());}
               processAndPublishFileEvent(mentor, degreeFile, "Degree");
           }
           if (otherFile!=null && !otherFile.isEmpty()) {
               if(mentor.getPublic_id_other()!=null){
                cloudinaryService.deletePdf(mentor.getPublic_id_other());}
              processAndPublishFileEvent(mentor, otherFile, "Other");
           }
           return mentor;
       }
    }

    private void processAndPublishFileEvent(Mentor mentor, MultipartFile file, String type) throws IOException {
        if (file != null && !file.isEmpty()) {
            String absolutePath = FileUtil.saveFile(file);
            File tempFile = FileUtil.getFileByPath(absolutePath);
            try {
                MultipartFile multipartFile = FileUtil.convertFileToMultipart(tempFile);
                applicationEventPublisher.publishEvent(new MentorEventDto(mentor, multipartFile, type));
            } finally {
                tempFile.delete();
            }
        }
    }

    @Override
    public MentorResponse getMentorById(int id) {
        if(mentorRepository.existsById(id)){
            return mentorMapper.toMentorResponse(mentorRepository.findById(id).get());
        }
        else {
            throw new CustomException("Mentor not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<MentorResponse> getAllMentors() {
        return mentorMapper.toMentorResponseList(mentorRepository.findAll());
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
