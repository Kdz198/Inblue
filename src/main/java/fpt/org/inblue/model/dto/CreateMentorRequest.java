package fpt.org.inblue.model.dto;

import fpt.org.inblue.model.enums.Role;
import jakarta.annotation.Nullable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMentorRequest {
MentorInfo data;
    MultipartFile avatar;
     MultipartFile identityFile;
    MultipartFile degreeFile;
     MultipartFile otherFile;
}
