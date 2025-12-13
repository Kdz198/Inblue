package fpt.org.inblue.model.dto;

import fpt.org.inblue.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCvDtoRequest {
    User user;
    MultipartFile file;
    String message;
}
