//package com.rapidquest.querysystem; // <-- Make sure this package matches yours!
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import java.util.List;
//
//@Slf4j // A Lombok annotation to add a logger, so we can see errors in the console
//@Service
//public class QueryService {
//
//    @Autowired
//    private QueryRepository queryRepository;
//    @Autowired
//    private EmailService emailService;
//    // Injects the values from application.properties
//    @Value("${gemini.api.key}")
//    private String apiKey;
//
//    @Value("${gemini.api.url}")
//    private String apiUrl;
//
//    // Helper for making API calls
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    // Helper for parsing JSON
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public List<Query> getAllQueries() {
//        // This is much more efficient!
//        return queryRepository.findAllByOrderByIdDesc();
//    }
//    /**
//     * This is the main logic method.
//     * It creates a new query, analyzes it with AI, and saves it.
//     */
//    /**
//     * This is the main logic method.
//     * It creates a new query, analyzes it with AI, and saves it.
//     */
//    public Query createNewQuery(String content, String source, String assignedTo) {
//
//        // 1. Get AI-powered analytics for the content
//        AiResponse aiData = getAiAnalytics(content);
//
//        // 2. Create a new Query object
//        Query newQuery = new Query();
//        newQuery.setContent(content);
//        newQuery.setSource(source);
//
//        // 3. Set properties from the AI response
//        newQuery.setCategory(aiData.getCategory());
//
//        Priority priority = Priority.valueOf(aiData.getPriority()); // Get priority from AI
//        newQuery.setPriority(priority);
//
//        // 4. SET STATUS AND RUN ESCALATION LOGIC
//        newQuery.setStatus("New");
//
//        if (priority == Priority.High) {
//            // --- THIS IS THE ESCALATION RULE ---
//            // If priority is High, override assignment and send to "Tech Team"
//            newQuery.setAssignedTo("Tech Team");
//            // We save first to get an ID for the query
//            Query savedQuery = queryRepository.save(newQuery);
//
//            // Send email using the saved query (which now has an ID)
//            emailService.sendEscalationNotification(savedQuery);
//            return savedQuery; // Return the saved query immediately
//        } else {
//            // Otherwise, just use the assignment from the form
//            newQuery.setAssignedTo(assignedTo);
//        }
//
//        // 5. Save and return the new query
//        return queryRepository.save(newQuery);
//    }
//
//    // --- DELETE METHOD ---
//    public void deleteQuery(Long id) {
//        queryRepository.deleteById(id);
//    }
//
//    // --- UPDATE STATUS METHOD ---
//    public Query updateQueryStatus(Long id, String status) {
//        // Find the existing query
//        Query query = queryRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Query not found with id: " + id));
//
//        // Update the status
//        query.setStatus(status);
//
//        // Save and return the updated query
//        return queryRepository.save(query);
//    }
//
//    // --- UPDATE ASSIGNMENT METHOD ---
//    public Query updateQueryAssignment(Long id, String assignedTo) {
//        Query query = queryRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Query not found with id: " + id));
//
//        // Update the assignment
//        query.setAssignedTo(assignedTo);
//
//        // Save and return
//        return queryRepository.save(query);
//    }
//
//    /**
//     * Tries to call the Gemini API. If it fails, it uses the simple fallback logic.
//     */
//    private AiResponse getAiAnalytics(String queryContent) {
//        try {
//            // This is the "God Prompt" that instructs the AI
//            String prompt = "You are a customer support triage system. Analyze the following query." +
//                    "Respond ONLY with a valid JSON object in the format {\"category\": \"...\", \"priority\": \"...\"}." +
//                    "The 'category' must be one of: 'Complaint', 'Question', 'Feature Request', 'Spam', or 'Greeting'." +
//                    "The 'priority' must be one of: 'High', 'Medium', or 'Low'." +
//                    "Query: \"" + queryContent + "\"";
//
//            // 1. Create the request body for Gemini API
//            GeminiRequest.Part part = new GeminiRequest.Part(prompt);
//            GeminiRequest.Content content = new GeminiRequest.Content(List.of(part));
//            GeminiRequest requestBody = new GeminiRequest(List.of(content));
//
//            // 2. Set headers
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            // 3. Create the full HTTP request
//            HttpEntity<GeminiRequest> entity = new HttpEntity<>(requestBody, headers);
//
//            // 4. Make the API call
//            String fullApiUrl = apiUrl + "?key=" + apiKey;
//            GeminiResponse response = restTemplate.postForObject(fullApiUrl, entity, GeminiResponse.class);
//
//            // 5. Extract the JSON text from the AI's response
//            String aiJsonText = response.getCandidates().get(0).getContent().getParts().get(0).getText();
//
//            // 6. Parse that text string into our AiResponse object
//            aiJsonText = aiJsonText.replace("```json", "").replace("```", "").trim();
//
//            return objectMapper.readValue(aiJsonText, AiResponse.class);
//
//        } catch (Exception e) {
//            // Log the error so we can see it in the console
//            log.error("Gemini API call failed: " + e.getMessage());
//            // IMPORTANT: If AI fails, use the fallback simulation
//            return getFallbackAnalytics(queryContent);
//        }
//    }
//
//    /**
//     * This is our "Simulated AI" from before.
//     * It's used as a fallback if the real AI call fails.
//     */
//    private AiResponse getFallbackAnalytics(String content) {
//        String category = "Question"; // Default
//        String priority = "Medium"; // Default
//
//        String lowerContent = content.toLowerCase();
//
//        if (lowerContent.contains("complaint") || lowerContent.contains("angry") ||
//                lowerContent.contains("crashed") || lowerContent.contains("ridiculous")) {
//            category = "Complaint";
//            priority = "High";
//        } if(lowerContent.trim().equals("hi") || lowerContent.trim().equals("hello")){
//            category = "Greeting";
//            priority = "Low";
//        }else if (lowerContent.contains("help") || lowerContent.contains("how to")) {
//            category = "Question";
//            priority = "Medium";
//        } else if (lowerContent.contains("idea") || lowerContent.contains("suggest")) {
//            category = "Request";
//            priority = "Low";
//        }
//
//        return new AiResponse(category, priority);
//    }
//
//
//    // --- DTO Helper Classes ---
//    // These static inner classes map to the JSON structure for the Gemini API call.
//
//    // DTO for the AI's *inner* JSON response (what we asked for)
//    @Data
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    private static class AiResponse {
//        private String category;
//        private String priority;
//
//        // Constructors for fallback
//        public AiResponse() {}
//        public AiResponse(String category, String priority) {
//            this.category = category;
//            this.priority = priority;
//        }
//    }
//
//    // DTOs for the *outer* Gemini API request
//    @Data
//    private static class GeminiRequest {
//        private final List<Content> contents;
//
//        @Data
//        private static class Content {
//            private final List<Part> parts;
//        }
//
//        @Data
//        private static class Part {
//            private final String text;
//        }
//    }
//
//    // DTOs for the *outer* Gemini API response
//    @Data
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    private static class GeminiResponse {
//        private List<Candidate> candidates;
//
//        @Data
//        @JsonIgnoreProperties(ignoreUnknown = true)
//        private static class Candidate {
//            private Content content;
//        }
//
//        @Data
//        @JsonIgnoreProperties(ignoreUnknown = true)
//        private static class Content {
//            private List<Part> parts;
//        }
//
//        @Data
//        @JsonIgnoreProperties(ignoreUnknown = true)
//        private static class Part {
//            private String text;
//        }
//    }
//}

package com.rapidquest.querysystem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class QueryService {

    @Autowired
    private QueryRepository queryRepository;

    @Autowired
    private EmailService emailService;


    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private ObjectMapper objectMapper;



    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;


    public List<Query> getAllQueries() {
        return queryRepository.findAllByOrderByIdDesc();
        // Or:
        // return queryRepository.findAll().stream()
        //        .sorted((q1, q2) -> q2.getId().compareTo(q1.getId()))
        //        .toList();
    }


    public Query createNewQuery(String content, String source, String assignedTo) {



        AiResponse aiData = getAiAnalytics(content);


        Query newQuery = new Query();
        newQuery.setCreatedAt(Instant.now());
        newQuery.setContent(content);
        newQuery.setSource(source);
        newQuery.setSuggestedReply(aiData.getSuggestedReply());

        newQuery.setCategory(aiData.getCategory());

        Priority priority = Priority.valueOf(aiData.getPriority());
        newQuery.setPriority(priority);


        newQuery.setStatus("New");

        if (priority == Priority.High) {

            newQuery.setAssignedTo("Tech Team");


            Query savedQuery = queryRepository.save(newQuery);
            emailService.sendEscalationNotification(savedQuery);
            return savedQuery;

        } else {

            newQuery.setAssignedTo(assignedTo);
        }


        return queryRepository.save(newQuery);
    }



    // --- DELETE METHOD ---
    public void deleteQuery(Long id) {
        queryRepository.deleteById(id);
    }

    // --- UPDATE STATUS METHOD ---
    public Query updateQueryStatus(Long id, String status) {
        Query query = queryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Query not found with id: " + id));


        if (status.equals("Resolved")) {
            query.setStatus("Resolved");
            query.setResolvedAt(Instant.now()); // Set the resolution time!
        } else {
            query.setStatus(status);
        }
        return queryRepository.save(query);
    }

    // --- UPDATE ASSIGNMENT METHOD ---
    public Query updateQueryAssignment(Long id, String assignedTo) {
        Query query = queryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Query not found with id: " + id));
        query.setAssignedTo(assignedTo);
        return queryRepository.save(query);
    }


    /**
     * Tries to call the Gemini API using the non-blocking WebClient.
     */
    private AiResponse getAiAnalytics(String queryContent) {
        try {
            // This new prompt asks for all 3 fields in one JSON object
            String prompt = "You are a customer support triage system. Analyze the following query." +
                    "Respond ONLY with a valid JSON object in the format {\"category\": \"...\", \"priority\": \"...\", \"suggestedReply\": \"...\"}." +
                    "The 'category' must be one of: 'Complaint', 'Question', 'Feature Request', or 'Spam'." +
                    "The 'priority' must be one of: 'High', 'Medium', or 'Low'." +
                    "For 'suggestedReply': Write a polite, helpful reply. If it's a complaint, be empathetic. Provide 1-2 simple troubleshooting steps. At the end, ALWAYS add the sentence: 'If these steps don't work, please reply and our technical team will assist you.' Keep the reply under 500 characters." +
                    "Query: \"" + queryContent + "\"";

            GeminiRequest.Part part = new GeminiRequest.Part(prompt);
            GeminiRequest.Content content = new GeminiRequest.Content(List.of(part));
            GeminiRequest requestBody = new GeminiRequest(List.of(content));

            String fullApiUrl = apiUrl + "?key=" + apiKey;
            WebClient webClient = webClientBuilder.build();

            GeminiResponse response = webClient.post()
                    .uri(fullApiUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            String aiJsonText = response.getCandidates().get(0).getContent().getParts().get(0).getText();
            aiJsonText = aiJsonText.replace("```json", "").replace("```", "").trim();

            // This will now parse all 3 fields into the AiResponse object
            return objectMapper.readValue(aiJsonText, AiResponse.class);

        } catch (Exception e) {
            log.error("Gemini API call failed: " + e.getMessage());
            // Use the fallback logic
            return getFallbackAnalytics(queryContent);
        }
    }

    /**
     * This is our "Simulated AI" fallback.
     */
    private AiResponse getFallbackAnalytics(String content) {
        String category = "Question"; // Default
        String priority = "Medium"; // Default

        // (Assuming you added the Priority enum)
        if (content.toLowerCase().contains("complaint") || content.toLowerCase().contains("angry")) {
            category = "Complaint";
            priority = "High";
        } else if (content.toLowerCase().contains("help")) {
            category = "Question";
            priority = "Medium";
        } else if (content.toLowerCase().contains("idea")) {
            category = "Feature Request"; // Or just "Request"
            priority = "Low";
        }

        return new AiResponse(category, priority);
    }


    // --- DTO Helper Classes ---
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AiResponse {
        private String category;
        private String priority;
        private String suggestedReply;
        public AiResponse() {}
        public AiResponse(String category, String priority) {
            this.category = category;
            this.priority = priority;
            this.suggestedReply = "We've received your query and will have an agent review it shortly.";
        }
    }

    @Data
    private static class GeminiRequest {
        private final List<Content> contents;
        @Data
        private static class Content {
            private final List<Part> parts;
        }
        @Data
        private static class Part {
            private final String text;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiResponse {
        private List<Candidate> candidates;
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Candidate {
            private Content content;
        }
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Content {
            private List<Part> parts;
        }
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Part {
            private String text;
        }
    }



}