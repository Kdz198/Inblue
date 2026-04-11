package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.model.MemberShipPlan;
import fpt.org.inblue.model.enums.Major;
import fpt.org.inblue.model.enums.Role;
import jakarta.persistence.*;
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
    private String university;
    private Major major;
    private String cvUrl;
    private String cv_public_id;
}
