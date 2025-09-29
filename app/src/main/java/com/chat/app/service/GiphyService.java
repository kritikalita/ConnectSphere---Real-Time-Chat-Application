package com.chat.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GiphyService {

    @Value("${giphy.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getRandomGifUrl(String query) {
        String url = String.format("https://api.giphy.com/v1/gifs/random?api_key=%s&tag=%s", apiKey, query);
        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(jsonResponse);
            // Navigate the JSON structure to find the URL
            return root.path("data").path("images").path("original").path("url").asText();
        } catch (Exception e) {
            // In case of an error or no GIF found, return null
            e.printStackTrace();
            return null;
        }
    }
}









