package fpt.org.inblue.service.impl;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.dto.CreateMentorRequest;
import fpt.org.inblue.model.dto.MentorEventDto;
import fpt.org.inblue.model.dto.MentorInfo;
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

    @Override
    public Mentor createMentor(MentorInfo data, MultipartFile identityFile, MultipartFile degreeFile, MultipartFile otherFile,MultipartFile avatar) throws IOException {
       if(data.getId()==null){
           CreateMentorRequest mentor = new CreateMentorRequest();
           MentorInfo mentorInfo = new MentorInfo(data.getName(), data.getEmail(), data.getPassword(), data.getBio(), data.getExpertise(), data.getYearsOfExperience(), data.getLinkedInUrl(), data.getCurrentCompany())
                   ;
           mentor.setData(mentorInfo);
           mentor.setAvatar(avatar);
           mentor.setIdentityFile(identityFile);
           mentor.setDegreeFile(degreeFile);
           mentor.setOtherFile(otherFile);

           Mentor mentorEntity = Mentor.builder()
                   .name(mentor.getData().getName())
                   .email(mentor.getData().getEmail())
                   .password(mentor.getData().getPassword())
                   .role(Role.MENTOR)
                   .isActive(false)
                   .bio(mentor.getData().getBio())
                   .expertise(mentor.getData().getExpertise())
                   .yearsOfExperience(mentor.getData().getYearsOfExperience())
                   .linkedInUrl(mentor.getData().getLinkedInUrl())
                   .currentCompany(mentor.getData().getCurrentCompany())
                   .rate(0)
                   .totalSession(0)
                   .build();
           Mentor savedMentor = mentorRepository.save(mentorEntity);
           // Save avatar file
           String absolutePath = FileUtil.saveFile(mentor.getAvatar());
           File file = FileUtil.getFileByPath(absolutePath);
           MultipartFile avata = FileUtil.convertFileToMultipart(file);
           file.delete();
           applicationEventPublisher.publishEvent(new MentorEventDto(savedMentor, avata,"avatar"));
           // Save identity file
           String absolutePath_1 = FileUtil.saveFile(mentor.getIdentityFile());
           File file_1 = FileUtil.getFileByPath(absolutePath_1);
           MultipartFile identity = FileUtil.convertFileToMultipart(file_1);
           file_1.delete();
           applicationEventPublisher.publishEvent(new MentorEventDto(savedMentor, identity,"IdentityCard"));

//        // Save degree file
           String absolutePath_2 = FileUtil.saveFile(mentor.getDegreeFile());
           File file_2 = FileUtil.getFileByPath(absolutePath_2);
           MultipartFile degree = FileUtil.convertFileToMultipart(file_2);
           file_2.delete();
           applicationEventPublisher.publishEvent(new MentorEventDto(savedMentor, degree,"Degree"));
//        // Save other file
           String absolutePath_3 = FileUtil.saveFile(mentor.getOtherFile());
           File file_3 = FileUtil.getFileByPath(absolutePath_3);
           MultipartFile other = FileUtil.convertFileToMultipart(file_3);
           file_3.delete();
           applicationEventPublisher.publishEvent(new MentorEventDto(savedMentor, other,"Other"));
           return savedMentor;
       }
       else{
           Mentor updateMentor = mentorRepository.findById(data.getId()).orElseThrow(() -> new RuntimeException("Mentor Not Found"));
           updateMentor.setName(data.getName());
           updateMentor.setEmail(data.getEmail());
           updateMentor.setBio(data.getBio());
           updateMentor.setExpertise(data.getExpertise());
           updateMentor.setYearsOfExperience(data.getYearsOfExperience());
           updateMentor.setLinkedInUrl(data.getLinkedInUrl());
           updateMentor.setCurrentCompany(data.getCurrentCompany());
           updateMentor.setPassword(data.getPassword());

            if(updateMentor.getAvatarUrl()!=null) {
                updateMentor.setAvatarUrl(updateMentor.getAvatarUrl());
                updateMentor.setPublic_id(updateMentor.getPublic_id());
            }
            if(updateMentor.getIdentityImg()!=null) {
                updateMentor.setIdentityImg(updateMentor.getIdentityImg());
                updateMentor.setPublic_id_identity(updateMentor.getPublic_id_identity());
            }
            if(updateMentor.getDegreeImg()!=null) {
                updateMentor.setDegreeImg(updateMentor.getDegreeImg());
                updateMentor.setPublic_id_degree(updateMentor.getPublic_id_degree());
            }
            if(updateMentor.getOtherFile()!=null) {
                updateMentor.setOtherFile(updateMentor.getOtherFile());
                updateMentor.setPublic_id_other(updateMentor.getPublic_id_other());
            }
           Mentor savedMentor = mentorRepository.save(updateMentor);
           if (!avatar.isEmpty()) {
               System.out.println("saved mentor " + savedMentor.getPublic_id());
               System.out.println("update mentor: " + updateMentor.getPublic_id());
               cloudinaryService.deleteImage(updateMentor.getPublic_id());
               String absolutePath = FileUtil.saveFile(avatar);
               File file = FileUtil.getFileByPath(absolutePath);
               MultipartFile avata = FileUtil.convertFileToMultipart(file);
               file.delete();
               applicationEventPublisher.publishEvent(new MentorEventDto(savedMentor, avata,"avatar"));
           }
           if (!identityFile.isEmpty()) {
               System.out.println("Deleting old identity file with public ID: " + savedMentor.getPublic_id_identity());
               cloudinaryService.deletePdf(updateMentor.getPublic_id_identity());
               String absolutePath = FileUtil.saveFile(identityFile);
               File file = FileUtil.getFileByPath(absolutePath);
               MultipartFile identity = FileUtil.convertFileToMultipart(file);
               file.delete();
               applicationEventPublisher.publishEvent(new MentorEventDto(savedMentor, identity,"IdentityCard"));
           }
           if (!degreeFile.isEmpty()) {
               System.out.println("Deleting old degree file with public ID: " + savedMentor.getPublic_id_degree());
               cloudinaryService.deletePdf(updateMentor.getPublic_id_degree());
               String absolutePath = FileUtil.saveFile(degreeFile);
               File file = FileUtil.getFileByPath(absolutePath);
               MultipartFile degree = FileUtil.convertFileToMultipart(file);
               file.delete();
               applicationEventPublisher.publishEvent(new MentorEventDto(savedMentor, degree,"Degree"));
           }
           if (!otherFile.isEmpty()) {
                cloudinaryService.deletePdf(updateMentor.getPublic_id_other());
               String absolutePath = FileUtil.saveFile(otherFile);
               File file = FileUtil.getFileByPath(absolutePath);
               MultipartFile other = FileUtil.convertFileToMultipart(file);
               file.delete();
               applicationEventPublisher.publishEvent(new MentorEventDto(savedMentor, other,"Other"));
           }
           return savedMentor;
       }
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
