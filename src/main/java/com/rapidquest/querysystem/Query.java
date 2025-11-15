package com.rapidquest.querysystem;
import jakarta.persistence.Column;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data; // Import Lombok

@Data // Lombok annotation to auto-create getters, setters, etc.
@Entity // JPA annotation to make this a database table
public class Query {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Use Identity for auto-increment
    private Long id;

    @Column(length = 2048)
    private String content;       // The message itself (e.g., "My app crashed!")
    private String source;        // "Email", "Social Media", "Chat"
    private Instant createdAt;
    private Instant resolvedAt;
    // These will be filled by the AI!
    private String category;      // "Complaint", "Question", "Request"

    // --- THIS IS THE FIX ---
    @Enumerated(EnumType.STRING)  // Tells JPA to store "High", "Medium", "Low" as a string
    private Priority priority;      // This now uses the Priority enum
    // -----------------------

    // For tracking
    private String status;        // "New", "In Progress", "Resolved"
    private String assignedTo;    // "Unassigned", "Support Team A"
    @Column(length = 1024) // Allow for a longer reply
    private String suggestedReply;
}