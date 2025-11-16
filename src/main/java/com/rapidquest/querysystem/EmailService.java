package com.rapidquest.querysystem;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {


    @Value("${spring.mail.username}")
    private String fromEmail;


    @Value("${escalation.email.to}")
    private String escalationEmailTo;


    @Value("${sendgrid.api.key}")
    private String sendgridApiKey;

    /**
     * Sends an escalation notification using SendGrid.
     */
    public void sendEscalationNotification(Query query) {
        // Create the email objects
        Email from = new Email(fromEmail);
        Email to = new Email(escalationEmailTo);
        String subject = "URGENT: High Priority Query Escalated (ID: " + query.getId() + ")";

        String emailBody = "A new query has been automatically escalated due to High priority:\n\n" +
                "Query ID: " + query.getId() + "\n" +
                "Category: " + query.getCategory() + "\n" +
                "Source: " + query.getSource() + "\n" +
                "Assigned To: " + query.getAssignedTo() + "\n\n" +
                "Content: \n" + query.getContent();

        Content content = new Content("text/plain", emailBody);
        Mail mail = new Mail(from, subject, to, content);


        SendGrid sg = new SendGrid(sendgridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            System.out.println("Escalation email sent! Status code: " + response.getStatusCode());

        } catch (IOException e) {
            System.err.println("Error sending escalation email via SendGrid: " + e.getMessage());
        }
    }

    /**
     * Sends an AI-generated auto-reply to the user using SendGrid.
     */
    public void sendAutoReply(String userEmail, String originalSubject, String aiReply) {
        // Create the email objects
        Email from = new Email(fromEmail);
        Email to = new Email(userEmail);
        String subject = "Re: " + originalSubject;
        Content content = new Content("text/plain", aiReply);
        Mail mail = new Mail(from, subject, to, content);

        // Send the email via SendGrid's HTTP API
        SendGrid sg = new SendGrid(sendgridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            System.out.println("Auto-reply sent! Status code: " + response.getStatusCode());

        } catch (IOException e) {
            System.err.println("Error sending auto-reply via SendGrid: " + e.getMessage());
        }
    }
}