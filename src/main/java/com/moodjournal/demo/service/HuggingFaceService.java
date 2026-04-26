package com.moodjournal.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class HuggingFaceService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    @Value("${huggingface.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public record MoodResult(String mood, double confidence) {}

    public MoodResult analyzeMood(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, String> body = Map.of("inputs", text);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

        try {
            // HuggingFace returns [[{label, score}, {label, score}]]
            JsonNode results = objectMapper.readTree(response.getBody()).get(0);
            JsonNode top = results.get(0); // highest confidence result
            return new MoodResult(top.get("label").asText(), top.get("score").asDouble());
        } catch (Exception e) {
            return new MoodResult("UNKNOWN", 0.0);
        }
    }
}