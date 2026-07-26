package fpt.org.inblue.model.dto;

import fpt.org.inblue.enums.Role;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    @Nullable
    private Integer id;

    private String name;

    @NotNull
    private String email;

    private String password;
    private Role role;

    private String phone;
    private String address;
    private String linkedInUrl;
}
