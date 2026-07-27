package fpt.org.inblue.service;

import fpt.org.inblue.model.CandidateProfile;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.UserInfo;
import fpt.org.inblue.model.dto.response.UserResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    List<User> getAll();

    User getById(int id);

    User createUser(UserInfo data, MultipartFile avatar) throws IOException;

    CandidateProfile upCv(int userId, MultipartFile cvFile) throws IOException;

    UserResponse getUserResponseById(int userId);
    UserResponse changePassword(String oldPass,String newPass);
}
