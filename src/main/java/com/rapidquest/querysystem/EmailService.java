package com.rapidquest.querysystem; // Or your package name

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;
    @Value("${escalation.email.to}") // <-- ADD THIS
    private String escalationEmailTo;
    /**
     * Sends an escalation notification.
     * @param query The query that triggered the escalation.
     */
    public void sendEscalationNotification(Query query) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@inboxpro.com"); // Can be any "from" address
            message.setTo(escalationEmailTo); // The person to notify
            message.setSubject("URGENT: High Priority Query Escalated (ID: " + query.getId() + ")");

            String text = "A new query has been automatically escalated due to High priority:\n\n" +
                    "Query ID: " + query.getId() + "\n" +
                    "Category: " + query.getCategory() + "\n" +
                    "Source: " + query.getSource() + "\n" +
                    "Assigned To: " + query.getAssignedTo() + "\n\n" +
                    "Content: \n" + query.getContent();

            message.setText(text);
            mailSender.send(message);

        } catch (Exception e) {
            // Log the error, but don't stop the main application flow
            System.err.println("Error sending escalation email: " + e.getMessage());
        }
    }
    public void sendAutoReply(String userEmail, String originalSubject, String aiReply) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("Re: " + originalSubject);
            message.setText(aiReply);

            mailSender.send(message);
            System.out.println("Auto-reply sent to: " + userEmail);

        } catch (Exception e) {
            System.err.println("Error sending auto-reply: " + e.getMessage());
        }
    }
}