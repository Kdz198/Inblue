package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.Role;
import fpt.org.inblue.model.CandidateProfile;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private int id;
    private String name;
    private String email;
    private Role role;
    private Boolean isActive;
    private String avatarUrl;
    private String public_id;
    private String cvUrl;
    private String cv_public_id;
    private List<CandidateProfile> candidates;
}
