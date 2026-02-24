package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.model.dto.request.PracticeQuestionRequest;
import lombok.Data;

import java.util.List;

@Data
public class PracticeAIResponse {
    List<List<PracticeQuestionRequest>> questions;
}
