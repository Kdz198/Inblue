package fpt.org.inblue.service.impl;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.constants.ApiPath;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.CandidateProfile;
import fpt.org.inblue.model.MemberShipPlan;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.UserUsage;
import fpt.org.inblue.model.dto.UserEventDto;
import fpt.org.inblue.model.dto.UserInfo;
import fpt.org.inblue.model.dto.response.CVParserResponse;
import fpt.org.inblue.model.dto.response.UserSubscriptionResponse;
import fpt.org.inblue.model.enums.*;
import fpt.org.inblue.repository.MemberShipPlanRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.repository.UserUsageRepository;
import fpt.org.inblue.service.CandidateProfileService;
import fpt.org.inblue.service.PythonApiClient;
import fpt.org.inblue.service.UserService;
import fpt.org.inblue.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
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
    @Autowired
    private MemberShipPlanRepository memberShipPlanRepository;
    @Autowired
    private UserUsageRepository userUsageRepository;

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
        MemberShipPlan memberShipPlan = memberShipPlanRepository.findByName(PlanName.NEW);
        if (user.getId() == null) {
            User userBuilder = User.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .role(Role.USER)
                    .isActive(true)
                    .university(user.getUniversity())
                    .major(Major.valueOf(user.getMajor()))
                    .membershipPlan(memberShipPlan)
                    .build();

            User savedUser = userRepository.save(userBuilder);
            if (avatar != null && !avatar.isEmpty()) {
                String absolutePath = FileUtil.saveFile(avatar);
                File file = FileUtil.getFileByPath(absolutePath);
                MultipartFile multipartFile = FileUtil.convertFileToMultipart(file);
                file.delete();
                applicationEventPublisher.publishEvent(new UserEventDto(savedUser, multipartFile, "avatar"));
            }
            return savedUser;
        } else {
            User updateUser = userRepository.findById(user.getId()).orElseThrow(() -> new RuntimeException("User Not Found"));
            updateUser.setName(user.getName());
            updateUser.setEmail(user.getEmail());
            updateUser.setUniversity(user.getUniversity());
            updateUser.setMajor(Major.valueOf(user.getMajor()));
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
            if (avatar != null && !avatar.isEmpty()) {
                if (updateUser.getPublic_id() != null) {
                    cloudinaryService.deleteImage(updateUser.getPublic_id());
                }
                String absolutePath = FileUtil.saveFile(avatar);
                File file = FileUtil.getFileByPath(absolutePath);
                MultipartFile multipartFile = FileUtil.convertFileToMultipart(file);
                file.delete();
                applicationEventPublisher.publishEvent(new UserEventDto(savedUser, multipartFile, "avatar"));
            }

            subscribePlan(savedUser.getId(), memberShipPlan.getId());
            return savedUser;
        }
    }

    @Override
    @Retryable(
            value = {Exception.class}, // Thử lại khi gặp bất kỳ ngoại lệ nào (hoặc cụ thể hơn như RestClientException)
            maxAttempts = 3,             // Tối đa 3 lần thử (1 lần chính + 2 lần retry)
            backoff = @Backoff(delay = 2000) // Mỗi lần thử lại cách nhau 2 giây
    )
    @Transactional
    public CandidateProfile upCv(int userId, MultipartFile cvFile) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found 123"));
        if (user.getCv_public_id() != null) {
            cloudinaryService.deletePdf(user.getCv_public_id());
        }
        int candidateId = 0;
        if (candidateProfileService.getProfileByUserId(userId) != null) {
            candidateId = candidateProfileService.getProfileByUserId(userId).getId();
        }
        CVParserResponse response =
                pythonApiClient.callApi(
                        PythonService.LLM,
                        ApiPath.CV_API,
                        HttpMethod.POST,
                        cvFile,
                        CVParserResponse.class);
        CandidateProfile candidateProfile = CandidateProfile.builder()
                .id(candidateId)
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
    @Transactional
    public User subscribePlan(int userId, int planId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User không tồn tại", HttpStatus.NOT_FOUND));
        MemberShipPlan plan = memberShipPlanRepository.findById(planId)
                .orElseThrow(() -> new CustomException("Gói không tồn tại", HttpStatus.NOT_FOUND));

        user.setMembershipPlan(plan);
        user = userRepository.save(user);

        // Reset usage khi đăng ký gói mới
        UserUsage usage = userUsageRepository.findByUser_Id(userId)
                .orElse(UserUsage.builder().user(user).build());
        usage.setAiInterviewUsed(0);
        usage.setPracticeSetUsed(0);
        usage.setQuizSetUsed(0);
        usage.setExpiredAt(LocalDate.now().plusDays(plan.getDurationDays()));
        userUsageRepository.save(usage);
        return user;
    }

    @Override
    public UserSubscriptionResponse getActiveSubscription(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User không tồn tại", HttpStatus.NOT_FOUND));

        MemberShipPlan plan = user.getMembershipPlan();
        UserUsage usage = userUsageRepository.findByUser_Id(userId)
                .orElse(UserUsage.builder().aiInterviewUsed(0).practiceSetUsed(0).quizSetUsed(0).build());

        return UserSubscriptionResponse.builder()
                .planName(plan.getName())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .maxAiInterview(plan.getMax_ai_interview())
                .maxPracticeSets(plan.getMax_practice_sets())
                .maxQuizSets(plan.getMax_quiz_sets())
                .aiInterviewUsed(usage.getAiInterviewUsed())
                .practiceSetUsed(usage.getPracticeSetUsed())
                .quizSetUsed(usage.getQuizSetUsed())
                .aiInterviewRemaining(Math.max(0, plan.getMax_ai_interview() - usage.getAiInterviewUsed()))
                .practiceSetRemaining(Math.max(0, plan.getMax_practice_sets() - usage.getPracticeSetUsed()))
                .quizSetRemaining(Math.max(0, plan.getMax_quiz_sets() - usage.getQuizSetUsed()))
                .isActive(true)
                .expiredAt(usage.getExpiredAt())
                .build();
    }

    @Override
    public void checkQuota(int userId,Feature checkFeature){
        MemberShipPlan plan = getPlan(userId);
        UserUsage usage = getOrCreateUsage(userId);
        if(usage.getExpiredAt().isBefore(LocalDate.now())){
            throw new CustomException("Hạn sử dụng gói đã hết. Vui lòng gia hạn hoặc nâng cấp gói.", HttpStatus.BAD_REQUEST);
        }
        switch(checkFeature){
            case Feature.AI_INTERVIEW :
                if (usage.getAiInterviewUsed() >= plan.getMax_ai_interview()) {
                    throw new CustomException(
                            "Bạn đã hết lượt phỏng vấn AI (" + plan.getMax_ai_interview() + "/" + plan.getMax_ai_interview() + "). Vui lòng nâng cấp gói.",
                            HttpStatus.BAD_REQUEST);
                }
                break;
            case Feature.PRACTICE_SET:
                if (usage.getPracticeSetUsed() >= plan.getMax_practice_sets()) {
                    throw new CustomException(
                            "Bạn đã hết lượt tạo bộ ôn luyện cá nhân hóa. Vui lòng nâng cấp gói.",
                            HttpStatus.BAD_REQUEST);
                }
                break;
            case Feature.QUIZ:
                if (usage.getQuizSetUsed() >= plan.getMax_quiz_sets()) {
                    throw new CustomException(
                            "Bạn đã hết lượt taọ bài kiểm tra. Vui lòng nâng cấp gói.",
                            HttpStatus.BAD_REQUEST);
                }
                break;
        }
    }
    @Override
    public void incrementUsage(int userId, Feature feature) {
        UserUsage usage = userUsageRepository.findByUser_Id(userId)
                .orElseThrow(() -> new CustomException("Không tìm thấy usage của user", HttpStatus.NOT_FOUND));
        switch (feature) {
            case Feature.AI_INTERVIEW:
                usage.setAiInterviewUsed(usage.getAiInterviewUsed() + 1);
                break;
            case Feature.PRACTICE_SET:
                usage.setPracticeSetUsed(usage.getPracticeSetUsed() + 1);
                break;
            case Feature.QUIZ:
                usage.setQuizSetUsed(usage.getQuizSetUsed() + 1);
                break;
        }
        userUsageRepository.save(usage);
    }

    private MemberShipPlan getPlan(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User không tồn tại", HttpStatus.NOT_FOUND));
        MemberShipPlan plan = user.getMembershipPlan();
        if (plan == null) {
            throw new CustomException("User chưa đăng ký gói nào. Vui lòng đăng ký gói để sử dụng.", HttpStatus.FORBIDDEN);
        }
        return plan;
    }

    private UserUsage getOrCreateUsage(int userId) {
        return userUsageRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new CustomException("User không tồn tại", HttpStatus.NOT_FOUND));
                    UserUsage newUsage = UserUsage.builder()
                            .user(user)
                            .aiInterviewUsed(0)
                            .practiceSetUsed(0)
                            .quizSetUsed(0)
                            .build();
                    return userUsageRepository.save(newUsage);
                });
    }
}
