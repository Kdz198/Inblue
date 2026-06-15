package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.model.CodingProblem;
import fpt.org.inblue.model.dto.request.CodingProblemGenerateRequest;
import fpt.org.inblue.model.dto.response.CodingProblemGenerateResponse;
import fpt.org.inblue.repository.CodingProblemsRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.CodingProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CodingProblemServiceImpl implements CodingProblemService {
    @Autowired
    private CodingProblemsRepository codingProblemsRepository;
    @Autowired
    private ApiClient apiClient;

    @Override
    public Optional<CodingProblem> findCodingProblemById(Long id) {
        return codingProblemsRepository.findById(id);
    }

    @Override
    public CodingProblem save(CodingProblem codingProblem) {
        return codingProblemsRepository.save(codingProblem);
    }

    @Override
    public List<CodingProblem> findAllCodingProblems() {
        return codingProblemsRepository.findAll();
    }

    @Override
    public CodingProblemGenerateResponse generateCodingProblem(CodingProblemGenerateRequest codingProblemGenerateRequest) {
        CodingProblemGenerateResponse response = apiClient.sendChatToAnythingLlm(
                AnythingLlmWorkspace.CODING_GEN,
                codingProblemGenerateRequest,
                "java", // sessionId có thể để null nếu không cần thiết
                true, // reset session để đảm bảo mỗi yêu cầu là độc lập
                null, // không có file nào cần gửi kèm
                CodingProblemGenerateResponse.class
        );
        System.out.println("Received response from LLM: " + response);
        return response ;
    }
}
