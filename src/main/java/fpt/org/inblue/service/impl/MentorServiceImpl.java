package fpt.org.inblue.service.impl;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.enums.Role;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.MentorMapper;
import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.MentorFeedback;
import fpt.org.inblue.model.dto.MentorEventDto;
import fpt.org.inblue.model.dto.request.ChangeMentorPasswordRequest;
import fpt.org.inblue.model.dto.request.CreateMentorRequest;
import fpt.org.inblue.model.dto.request.UpdateMentorRequest;
import fpt.org.inblue.model.dto.response.MentorFeedbackResponse;
import fpt.org.inblue.model.dto.response.MentorResponse;
import fpt.org.inblue.repository.MentorFeedbackRepository;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.service.MentorService;
import fpt.org.inblue.utils.FileUtil;
import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MentorServiceImpl implements MentorService {
    private final MentorRepository mentorRepository;
    private final MentorFeedbackRepository mentorFeedbackRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CloudinaryService cloudinaryService;
    private final MentorMapper mentorMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MentorResponse createMentor(CreateMentorRequest data, MultipartFile avatar) throws IOException {
        Mentor mentor = mentorMapper.toEntity(data);
        if (data.getPassword() != null && !data.getPassword().isEmpty()) {
            mentor.setPassword(passwordEncoder.encode(data.getPassword()));
        }
        mentor.setRole(Role.MENTOR);
        mentor.setActive(true);
        mentor.setTotalSession(0);
        mentor.setAverageRating(0);
        if (data.getPricePerMinute() != null) {
            mentor.setPricePerMinute(data.getPricePerMinute());
        } else {
            mentor.setPricePerMinute(0);
        }
        mentor = mentorRepository.save(mentor);
        processAndPublishFileEvent(mentor, avatar, "avatar");
        return toMentorResponse(mentor);
    }

    @Override
    public MentorResponse updateMentor(int id, UpdateMentorRequest data, MultipartFile avatar) throws IOException {
        Mentor mentor = mentorRepository
                .findById(id)
                .orElseThrow(() -> new CustomException("Mentor Not Found", HttpStatus.NOT_FOUND));

        mentorMapper.updateMentorFromDto(data, mentor);

        mentor = mentorRepository.save(mentor);
        if (avatar != null && !avatar.isEmpty()) {
            if (mentor.getPublic_id() != null) {
                cloudinaryService.deleteImage(mentor.getPublic_id());
            }
            processAndPublishFileEvent(mentor, avatar, "avatar");
        }
        return toMentorResponse(mentor);
    }

    @Override
    public MentorResponse changePassword(int id, ChangeMentorPasswordRequest request) {
        Mentor mentor = mentorRepository
                .findById(id)
                .orElseThrow(() -> new CustomException("Mentor Not Found", HttpStatus.NOT_FOUND));

        if (request.getOldPassword() != null && !request.getOldPassword().isEmpty()) {
            if (!passwordEncoder.matches(request.getOldPassword(), mentor.getPassword())) {
                throw new CustomException("Mật khẩu cũ không đúng", HttpStatus.BAD_REQUEST);
            }
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new CustomException("Mật khẩu mới không được để trống", HttpStatus.BAD_REQUEST);
        }

        mentor.setPassword(passwordEncoder.encode(request.getNewPassword()));
        mentor = mentorRepository.save(mentor);
        return toMentorResponse(mentor);
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
            return toMentorResponse(mentorRepository.findById(id).get());
        } else {
            throw new CustomException("Mentor not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<MentorResponse> getAllMentors() {
        return toMentorResponseList(mentorRepository.findAll());
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

    private MentorResponse toMentorResponse(Mentor mentor) {
        MentorResponse response = mentorMapper.toMentorResponse(mentor);
        List<MentorFeedback> feedbacks = mentorFeedbackRepository.findAllByMentor_Id(mentor.getId());
        List<MentorFeedbackResponse> feedbackResponses = feedbacks.stream()
                .map(fb -> MentorFeedbackResponse.builder()
                        .rating(fb.getRating())
                        .comment(fb.getComment())
                        .userName(fb.getUser() != null ? fb.getUser().getName() : null)
                        .userAvatarUrl(fb.getUser() != null ? fb.getUser().getAvatarUrl() : null)
                        .build())
                .toList();
        response.setFeedbacks(feedbackResponses);
        return response;
    }

    private List<MentorResponse> toMentorResponseList(List<Mentor> mentors) {
        return mentors.stream().map(this::toMentorResponse).toList();
    }
}
