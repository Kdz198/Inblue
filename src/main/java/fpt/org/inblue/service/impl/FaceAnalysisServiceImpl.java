package fpt.org.inblue.service.impl;

import fpt.org.inblue.model.dto.response.FaceAnalysisResponse;
import fpt.org.inblue.service.FaceAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FaceAnalysisServiceImpl implements FaceAnalysisService {

    private final RestTemplate restTemplate;
    private final String PYTHON_SERVICE_URL = "ENV_HERE";

    @Override
    public FaceAnalysisResponse analyzeFace(MultipartFile image) {
        return null;
    }
}
