package com.example.bossbot.ai.service;

import com.example.bossbot.ai.config.OllamaConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OllamaEmbeddingService Tests")
class OllamaEmbeddingServiceImplTest {

    @Mock
    private OllamaHttpClient httpClient;

    private OllamaEmbeddingServiceImpl service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        OllamaConfig config = new OllamaConfig();
        config.setModel("nomic-embed-text");
        service = new OllamaEmbeddingServiceImpl(config, httpClient);
    }

    @Test
    @DisplayName("Should parse embedding response correctly")
    void testEmbed_returnsEmbedding() throws Exception {
        JsonNode response = objectMapper.readTree("{\"embeddings\":[[0.1,0.2,0.3]]}");
        when(httpClient.post(eq("/api/embed"), any())).thenReturn(response);

        double[] result = service.embed("test text");

        assertThat(result).containsExactly(0.1, 0.2, 0.3);
    }

    @Test
    @DisplayName("Should parse batch embedding response correctly")
    void testEmbedBatch_returnsAllEmbeddings() throws Exception {
        JsonNode response = objectMapper.readTree("{\"embeddings\":[[0.1,0.2],[0.3,0.4],[0.5,0.6]]}");
        when(httpClient.post(eq("/api/embed"), any())).thenReturn(response);

        List<double[]> results = service.embedBatch(List.of("text1", "text2", "text3"));

        assertThat(results).hasSize(3);
        assertThat(results.get(0)).containsExactly(0.1, 0.2);
        assertThat(results.get(1)).containsExactly(0.3, 0.4);
        assertThat(results.get(2)).containsExactly(0.5, 0.6);
    }

    @Test
    @DisplayName("Should throw on non-200 status code")
    void testEmbed_httpError() {
        when(httpClient.post(eq("/api/embed"), any()))
                .thenThrow(new OllamaException("Ollama returned status 500: Internal Server Error"));

        assertThatThrownBy(() -> service.embed("test"))
                .isInstanceOf(OllamaException.class)
                .hasMessageContaining("Ollama returned status 500");
    }

    @Test
    @DisplayName("Should throw on missing embeddings field")
    void testEmbed_invalidJson() throws Exception {
        JsonNode response = objectMapper.readTree("{\"error\":\"model not found\"}");
        when(httpClient.post(eq("/api/embed"), any())).thenReturn(response);

        assertThatThrownBy(() -> service.embed("test"))
                .isInstanceOf(OllamaException.class)
                .hasMessageContaining("missing 'embeddings' array");
    }

    @Test
    @DisplayName("Should throw on connection failure")
    void testEmbed_connectionFailure() {
        when(httpClient.post(eq("/api/embed"), any()))
                .thenThrow(new OllamaException("Failed to call Ollama /api/embed",
                        new java.net.ConnectException("Connection refused")));

        assertThatThrownBy(() -> service.embed("test"))
                .isInstanceOf(OllamaException.class);
    }
}
