package fpt.org.inblue.service;

import fpt.org.inblue.model.dto.response.FaceAnalysisResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FaceAnalysisService {

    FaceAnalysisResponse analyzeFace(MultipartFile image);
}
