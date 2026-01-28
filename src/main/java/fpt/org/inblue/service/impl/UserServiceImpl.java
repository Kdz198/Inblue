package fpt.org.inblue.service.impl;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.constants.ApiPath;
import fpt.org.inblue.model.CandidateProfile;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.UserEventDto;
import fpt.org.inblue.model.dto.UserInfo;
import fpt.org.inblue.model.dto.response.CVParserResponse;
import fpt.org.inblue.model.enums.Role;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.CandidateProfileService;
import fpt.org.inblue.service.PythonApiClient;
import fpt.org.inblue.service.UserService;
import fpt.org.inblue.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private PythonApiClient pythonApiClient;
    @Autowired
    private CandidateProfileService candidateProfileService;

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User getById(int id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User Not Found"));
    }

    @Override
    public User createUser(UserInfo user, MultipartFile avatar) throws IOException {
        if (user.getId() == null) {
            User userBuilder = User.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .role(Role.USER)
                    .isActive(true)
                    .university(user.getUniversity())
                    .major(user.getMajor())
                    .build();
            User savedUser = userRepository.save(userBuilder);
            String absolutePath = FileUtil.saveFile(avatar);
            File file = FileUtil.getFileByPath(absolutePath);
            MultipartFile multipartFile = FileUtil.convertFileToMultipart(file);
            file.delete();
            applicationEventPublisher.publishEvent(new UserEventDto(savedUser, multipartFile, "avatar"));
            return savedUser;
        } else {
            User updateUser = userRepository.findById(user.getId()).orElseThrow(() -> new RuntimeException("User Not Found"));
            updateUser.setName(user.getName());
            updateUser.setEmail(user.getEmail());
            updateUser.setUniversity(user.getUniversity());
            updateUser.setMajor(user.getMajor());
            updateUser.setPassword(user.getPassword());
            if (updateUser.getAvatarUrl() != null) {
                updateUser.setAvatarUrl(updateUser.getAvatarUrl());
                updateUser.setPublic_id(updateUser.getPublic_id());
            }
            if (updateUser.getCvUrl() != null) {
                updateUser.setCvUrl(updateUser.getCvUrl());
                updateUser.setCv_public_id(updateUser.getCv_public_id());
            }
            User savedUser = userRepository.save(updateUser);
            if (!avatar.isEmpty()) {
                if (updateUser.getPublic_id() != null) {
                    cloudinaryService.deleteImage(updateUser.getPublic_id());
                }
                String absolutePath = FileUtil.saveFile(avatar);
                File file = FileUtil.getFileByPath(absolutePath);
                MultipartFile multipartFile = FileUtil.convertFileToMultipart(file);
                file.delete();
                applicationEventPublisher.publishEvent(new UserEventDto(savedUser, multipartFile, "avatar"));
            }
            return savedUser;
        }
    }

    public void uploadPdf(User user, MultipartFile file) throws IOException {
        Map<String, String> map = cloudinaryService.uploadDocument(file);
        user.setCvUrl(map.get("secure_url"));
        user.setCv_public_id(map.get("public_id"));
        userRepository.save(user);
    }

    @Override
    @Retryable(
            value = {Exception.class}, // Thử lại khi gặp bất kỳ ngoại lệ nào (hoặc cụ thể hơn như RestClientException)
            maxAttempts = 3,             // Tối đa 3 lần thử (1 lần chính + 2 lần retry)
            backoff = @Backoff(delay = 2000) // Mỗi lần thử lại cách nhau 2 giây
    )
    @Transactional
    public CandidateProfile upCv(int userId, MultipartFile cvFile) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));
        if (user.getCv_public_id() != null) {
            cloudinaryService.deletePdf(user.getCv_public_id());
        }
        if (candidateProfileService.getProfileByUserId(userId) != null) {
            candidateProfileService.deleteProfile(userId);
        }
        CVParserResponse response =
                pythonApiClient.callApi(
                        ApiPath.CV_API,
                        HttpMethod.POST,
                        cvFile,
                        CVParserResponse.class);
        CandidateProfile candidateProfile = CandidateProfile.builder()
                .id(0)
                .user(user)
                .targetRole(response.getTargetRole())
                .targetLevel(response.getTargetLevel())
                .introduction(response.getIntroduction())
                .technicalSkills(response.getTechnicalSkills())
                .softSkills(response.getSoftSkills())
                .tools(response.getTools())
                .projects(mapProjects(response.getProjects()))
                .workExperiences(mapWorkExperiences(response.getWorkExperiences()))
                .educations(mapEducations(response.getEducations()))
                .certifications(response.getCertifications())
                .achievements(response.getAchievements())
                .createdAt(LocalDateTime.now())
                .build();
        //publish event
        String absolutePath = FileUtil.saveFile(cvFile);
        File file = FileUtil.getFileByPath(absolutePath);
        MultipartFile multipartFile = FileUtil.convertFileToMultipart(file);
        file.delete();

        //event
        var profile = candidateProfileService.createProfile(candidateProfile);
        applicationEventPublisher.publishEvent(new UserEventDto(user, multipartFile, "cv"));
        return profile;
    }

    @Recover
    public CandidateProfile recover(Exception e, int userId, MultipartFile cvFile) {
        System.err.println("Retry failed for User ID " + userId + ". Error: " + e.getMessage());
        throw new RuntimeException("Dịch vụ phân tích CV hiện không khả dụng, vui lòng thử lại sau.");
    }

    private Double parseGpa(String gpaStr) {
        if (gpaStr == null || gpaStr.isEmpty()) {
            return 0.0;
        }

        try {
            String[] parts = gpaStr.split("/");
            if (parts.length < 2) {
                return Double.parseDouble(parts[0].trim());
            }

            double score = Double.parseDouble(parts[0].trim());
            double scale = Double.parseDouble(parts[1].trim());

            if (scale == 10.0) {
                return score * 0.4; // Quy đổi hệ 10 sang hệ 4
            } else if (scale == 4.0) {
                return score; // Giữ nguyên hệ 4
            }
            return score;

        } catch (Exception e) {
            System.err.println("Lỗi parse GPA: " + gpaStr + " - " + e.getMessage());
            return 0.0;
        }
    }

    private List<CandidateProfile.ProjectDetail> mapProjects(List<CVParserResponse.ProjectDTO> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(dto -> CandidateProfile.ProjectDetail.builder()
                        .name(dto.getName())
                        .description(dto.getDescription())
                        .role(dto.getRole())
                        .teamSize(dto.getTeamSize())
                        .usedTools(dto.getUsedTools())
                        .outcome(dto.getOutcome())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CandidateProfile.WorkExperience> mapWorkExperiences(List<CVParserResponse.WorkExperienceDTO> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(dto -> CandidateProfile.WorkExperience.builder()
                        .company(dto.getCompany())
                        .position(dto.getPosition())
                        .description(dto.getDescription())
                        .startDate(dto.getStartDate())
                        .endDate(dto.getEndDate())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CandidateProfile.EducationEntry> mapEducations(List<CVParserResponse.EducationDTO> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(dto -> CandidateProfile.EducationEntry.builder()
                        .school(dto.getSchool())
                        .major(dto.getMajor())
                        .degree(dto.getDegree())
                        .gpa(parseGpa(dto.getGpa()).toString())
                        .startDate(dto.getStartDate())
                        .endDate(dto.getEndDate())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean deleteUser(int id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }


}
