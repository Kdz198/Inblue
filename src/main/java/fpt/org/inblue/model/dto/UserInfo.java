package fpt.org.inblue.model.dto;

import jakarta.annotation.Nullable;
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
    private String email;
    private String password;
    private String university;
    private String major;
}
