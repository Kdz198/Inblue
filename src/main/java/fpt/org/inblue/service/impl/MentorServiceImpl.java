package fpt.org.inblue.service.impl;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.enums.Role;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.MentorMapper;
import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.dto.MentorEventDto;
import fpt.org.inblue.model.dto.MentorInfo;
import fpt.org.inblue.model.dto.response.MentorResponse;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.service.MentorService;
import fpt.org.inblue.utils.FileUtil;
import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MentorServiceImpl implements MentorService {
    private final MentorRepository mentorRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CloudinaryService cloudinaryService;
    private final MentorMapper mentorMapper;

    @Override
    public Mentor createMentor(MentorInfo data, MultipartFile avatar) throws IOException {
        Mentor mentor;
        if (data.getId() == null) {
            mentor = mentorMapper.toEntity(data);
            mentor.setRole(Role.MENTOR);
            mentor.setActive(false);
            mentor.setTotalSession(0);
            mentor.setAverageRating(0);
            mentor.setPricePerMinute(data.getPricePerMinute());
            mentor = mentorRepository.save(mentor);
            processAndPublishFileEvent(mentor, avatar, "avatar");
            return mentor;
        } else {
            mentor =
                    mentorRepository.findById(data.getId()).orElseThrow(() -> new RuntimeException("Mentor Not Found"));
            mentorMapper.updateMentorFromDto(data, mentor);

            if (mentor.getAvatarUrl() != null) {
                mentor.setAvatarUrl(mentor.getAvatarUrl());
                mentor.setPublic_id(mentor.getPublic_id());
            }
            mentor = mentorRepository.save(mentor);
            if (avatar != null && !avatar.isEmpty()) {
                if (mentor.getPublic_id() != null) {
                    cloudinaryService.deleteImage(mentor.getPublic_id());
                }
                processAndPublishFileEvent(mentor, avatar, "avatar");
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
        if (mentorRepository.existsById(id)) {
            return mentorMapper.toMentorResponse(mentorRepository.findById(id).get());
        } else {
            throw new CustomException("Mentor not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<MentorResponse> getAllMentors() {
        return mentorMapper.toMentorResponseList(mentorRepository.findAll());
    }

    @Override
    public void toggleActive(int id) {
        if (mentorRepository.existsById(id)) {
            Mentor mentor = mentorRepository.findById(id).get();
            if (mentor.isActive()) {
                mentor.setActive(false);
                mentorRepository.save(mentor);
            } else {
                mentor.setActive(true);
                mentorRepository.save(mentor);
            }
        } else {
            throw new CustomException("Mentor not found", HttpStatus.NOT_FOUND);
        }
    }
}
