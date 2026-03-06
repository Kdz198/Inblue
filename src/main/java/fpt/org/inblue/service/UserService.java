package fpt.org.inblue.service;

import fpt.org.inblue.model.CandidateProfile;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.UserInfo;
import fpt.org.inblue.model.dto.response.UserSubscriptionResponse;
import fpt.org.inblue.model.enums.Feature;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
     List<User> getAll();
     User getById(int id);
     User createUser(UserInfo data, MultipartFile avatar) throws IOException;

     CandidateProfile upCv(int userId, MultipartFile cvFile) throws IOException;

    // Subscription related methods
    User subscribePlan(int userId, int planId);
    UserSubscriptionResponse getActiveSubscription(int userId);
    void incrementUsage(int userId, Feature feature);
    void checkQuota(int userId,Feature checkFeature);
}
