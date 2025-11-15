package com.rapidquest.querysystem; // <-- Make sure this package matches yours!

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// This annotation turns our controller into a professional JSON API
@RestController
@RequestMapping("/api") // All our API routes will start with this
public class QueryController {

    @Autowired
    private QueryService queryService;
    @Autowired
    private EmailService emailService;
    // This method GETS all queries as JSON
    @GetMapping("/queries")
    public ResponseEntity<List<Query>> getAllQueries() {

        return ResponseEntity.ok(queryService.getAllQueries());
    }


    @PostMapping("/queries")
    public ResponseEntity<Query> createQuery(@RequestBody CreateQueryRequest request) {
        // Pass all three fields to the service
        Query newQuery = queryService.createNewQuery(request.getContent(), request.getSource(), request.getAssignedTo());
        return ResponseEntity.ok(newQuery);
    }


    @DeleteMapping("/queries/{id}")
    public ResponseEntity<Void> deleteQuery(@PathVariable Long id) {
        queryService.deleteQuery(id);
        return ResponseEntity.ok().build(); // Sends a 200 OK response
    }



    // --- FEATURE: UPDATE a Query's Status ---
    // This method UPDATES a query's status
    @PutMapping("/queries/{id}/status")
    public ResponseEntity<Query> updateQueryStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        Query updatedQuery = queryService.updateQueryStatus(id, request.getStatus());
        return ResponseEntity.ok(updatedQuery);
    }

    // --- FEATURE: UPDATE a Query's Assignment ---
    @PutMapping("/queries/{id}/assignment")
    public ResponseEntity<Query> updateQueryAssignment(
            @PathVariable Long id,
            @RequestBody UpdateAssignmentRequest request) {
        Query updatedQuery = queryService.updateQueryAssignment(id, request.getAssignedTo());
        return ResponseEntity.ok(updatedQuery);
    }
    @PostMapping("/chat-submit")
    public ResponseEntity<Void> submitChatQuery(@RequestBody ChatSubmitRequest request) {

        // 1. Create the query (this also generates the AI reply)
        Query newQuery = queryService.createNewQuery(
                request.getContent(),
                "Chat",               // Hard-code the source
                "Unassigned"          // Let escalation handle assignment
        );

        // 2. Get the AI-generated reply that was saved to the query
        String aiReply = newQuery.getSuggestedReply();

        // 3. Send the auto-reply to the user's provided email
        emailService.sendAutoReply(
                request.getEmail(),
                "Re: Your Chat Query",
                aiReply
        );

        return ResponseEntity.ok().build();
    }



    // --- Helper Classes (DTOs) for clean JSON requests ---
    static class ChatSubmitRequest {
        private String content;
        private String email;
        // getters and setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    static class CreateQueryRequest {
        private String content;
        private String source;
        private String assignedTo; // <-- ADD THIS LINE

        // getters and setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        // <-- ADD THESE GETTERS AND SETTERS -->
        public String getAssignedTo() { return assignedTo; }
        public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    }

    static class UpdateStatusRequest {
        private String status;
        // getter and setter
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // --- Helper Class for the new JSON request ---
    static class UpdateAssignmentRequest {
        private String assignedTo;
        // getter and setter
        public String getAssignedTo() { return assignedTo; }
        public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    }


}