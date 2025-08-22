package com.example.aitoolforfeedbackresolution.controllers;

import com.example.aitoolforfeedbackresolution.services.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GeminiController{

    // Inject the API key from environment variables or application.properties
    @Value("${GOOGLE_API_KEY}")
    private String geminiApiKey;

    @Autowired
    private GeminiService geminiService;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/ask-gemini-flash")
    public ResponseEntity<String> askGeminiFlash(@RequestBody String userPrompt) {
        // Basic validation for API key
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            return new ResponseEntity<>("Gemini API key is not configured.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Define the specific Gemini 2.0 Flash model endpoint
        // The API key is added as a query parameter in the URL, which is common for some Google APIs.
        // However, the curl command specified X-goog-api-key header. Let's use that for consistency.
        String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

        // Prepare request headers as specified in the curl command
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Add the API key to the 'X-goog-api-key' header
        headers.set("X-goog-api-key", geminiApiKey);

        // Prepare the request body (JSON payload for Gemini API)
        // This structure matches the 'contents' array with 'parts' and 'text'
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", userPrompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", Arrays.asList(textPart)); // List containing one text part

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Arrays.asList(content)); // List containing one content object

        // Create the HttpEntity which combines headers and body for the request
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Make the POST request to the Gemini API
            ResponseEntity<Map> geminiResponse = restTemplate.exchange(
                    geminiApiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class // Expecting a Map to represent the raw JSON response
            );

            // Process Gemini's response to extract the generated text
            if (geminiResponse.getStatusCode() == HttpStatus.OK && geminiResponse.getBody() != null) {
                Map<String, Object> responseBody = geminiResponse.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");

                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> contentMap = (Map<String, Object>) firstCandidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");

                    if (parts != null && !parts.isEmpty()) {
                        String generatedText = (String) parts.get(0).get("text");
                        return new ResponseEntity<>(generatedText, HttpStatus.OK);
                    }
                }
                // If no content is found or structure is unexpected
                return new ResponseEntity<>("No valid content found in Gemini response.", HttpStatus.NO_CONTENT);
            } else {
                // Handle non-200 HTTP responses from Gemini API
                return new ResponseEntity<>("Error calling Gemini API: " + geminiResponse.getStatusCode() + " - " + geminiResponse.getBody(), geminiResponse.getStatusCode());
            }

        } catch (Exception e) {
            // Catch any exceptions during the API call (e.g., network issues, malformed URL)
            System.err.println("Exception calling Gemini API: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            return new ResponseEntity<>("An internal error occurred while communicating with Gemini API: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testSql() {
        String query = "SELECT ERROR_MESSAGE, FAIL_REASN, RESPONSE_CODE FROM LOG_ERROR WHERE XML LIKE \"%47719642%\"";
        String data = geminiService.getData(query);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
