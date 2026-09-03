package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.dto.request.PineconeEmbeddingRequest;
import fpt.org.inblue.model.dto.response.PineconeEmbeddingResponse;
import fpt.org.inblue.service.EmbeddingService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PineconeEmbeddingServiceImpl implements EmbeddingService {
    private final RestClient restClient;
    private final String apiKey;
    private final String embeddingModel;
    private final int embeddingDimension;

    public PineconeEmbeddingServiceImpl(
            @Value("${PINECONE_EMBEDDING_URL:https://api.pinecone.io/embed}") String embeddingUrl,
            @Value("${PINECONE_API_KEY:}") String apiKey,
            @Value("${PINECONE_EMBEDDING_MODEL:llama-text-embed-v2}") String embeddingModel,
            @Value("${PINECONE_EMBEDDING_DIMENSION:384}") int embeddingDimension) {
        this.restClient = RestClient.builder().baseUrl(embeddingUrl).build();
        this.apiKey = apiKey;
        this.embeddingModel = embeddingModel;
        this.embeddingDimension = embeddingDimension;
    }

    @Override
    public float[] generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            throw new CustomException("Embedding text không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new CustomException("PINECONE_API_KEY chưa được cấu hình", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        PineconeEmbeddingRequest request = PineconeEmbeddingRequest.builder()
                .model(embeddingModel)
                .parameters(PineconeEmbeddingRequest.Parameters.builder()
                        .inputType("passage")
                        .truncate("END")
                        .dimension(embeddingDimension)
                        .build())
                .inputs(List.of(PineconeEmbeddingRequest.Input.builder().text(text).build()))
                .build();

        PineconeEmbeddingResponse response = restClient.post()
                .header("Api-Key", apiKey)
                .header("X-Pinecone-Api-Version", "2026-04")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PineconeEmbeddingResponse.class);

        if (response == null
                || response.getData() == null
                || response.getData().isEmpty()
                || response.getData().get(0).getValues() == null) {
            throw new CustomException("Pinecone không trả về embedding hợp lệ", HttpStatus.BAD_GATEWAY);
        }

        List<Double> values = response.getData().getFirst().getValues();
        if (values.size() != embeddingDimension) {
            throw new CustomException(
                    "Embedding dimension không khớp: expected " + embeddingDimension + ", actual " + values.size(),
                    HttpStatus.BAD_GATEWAY);
        }

        float[] embedding = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            embedding[i] = values.get(i).floatValue();
        }
        return embedding;
    }
}
