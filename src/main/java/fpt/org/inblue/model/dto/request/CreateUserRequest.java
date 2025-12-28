package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.dto.UserInfo;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    UserInfo data;
    @Nullable
    MultipartFile avatar ;
    @Nullable
    MultipartFile cvFile;
}
