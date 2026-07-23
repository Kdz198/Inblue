package fpt.org.inblue.model.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignMentorsRequestDto {
    private List<Integer> mentorIds;
}
