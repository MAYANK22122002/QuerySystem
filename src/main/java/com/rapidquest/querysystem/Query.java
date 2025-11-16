package com.rapidquest.querysystem;
import jakarta.persistence.Column;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Query {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2048)
    private String content;
    private String source;
    private Instant createdAt;
    private Instant resolvedAt;
    // These will be filled by the AI!
    private String category;


    @Enumerated(EnumType.STRING)
    private Priority priority;



    private String status;
    private String assignedTo;
    @Column(length = 1024)
    private String suggestedReply;
}