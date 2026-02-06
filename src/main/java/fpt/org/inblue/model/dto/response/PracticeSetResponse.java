package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.model.PracticeSet;
import fpt.org.inblue.model.PracticeSetItem;
import lombok.Data;

import java.util.List;

@Data

public class PracticeSetResponse {
    PracticeSet practiceSet;
    List<PracticeSetItem> practiceSetItem;
}
