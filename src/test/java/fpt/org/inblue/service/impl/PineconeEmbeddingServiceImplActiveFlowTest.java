package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fpt.org.inblue.exception.CustomException;
import org.junit.jupiter.api.Test;

class PineconeEmbeddingServiceImplActiveFlowTest {
    @Test
    void generateEmbeddingRejectsBlankText() {
        PineconeEmbeddingServiceImpl service =
                new PineconeEmbeddingServiceImpl("https://example.invalid", "key", "model", 384);
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.generateEmbedding(" "))
                        .getStatus()
                        .value());
    }

    @Test
    void generateEmbeddingRejectsMissingApiKeyBeforeNetworkCall() {
        PineconeEmbeddingServiceImpl service =
                new PineconeEmbeddingServiceImpl("https://example.invalid", "", "model", 384);
        assertEquals(
                500,
                assertThrows(CustomException.class, () -> service.generateEmbedding("Java"))
                        .getStatus()
                        .value());
    }
}
