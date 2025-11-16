package com.rapidquest.querysystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.FlagTerm;
import java.io.IOException;
import java.util.Properties;

@Service
public class EmailPollingService {

    @Autowired
    private QueryService queryService;

    @Autowired
    private EmailService emailService;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.imap.host}")
    private String host;

    @Scheduled(fixedRate = 60000)
    public void pollForEmails() {
        System.out.println("Checking for new emails...");
        try {
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imaps");

            Session session = Session.getInstance(properties, null);
            Store store = session.getStore("imaps");

            store.connect(host, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            if (messages.length == 0) {
                System.out.println("No new emails found.");
                inbox.close(false);
                store.close();
                return;
            }

            for (Message message : messages) {
                try {
                    String subject = message.getSubject();
                    String fromEmail = ((InternetAddress) message.getFrom()[0]).getAddress();


                    String emailContent = getTextFromMessage(message);
                    // --------------------------

                    System.out.println("New email found from: " + fromEmail + " Subject: " + subject);


                    Query newQuery = queryService.createNewQuery(emailContent, "Email", "Unassigned");


                    String aiReply = newQuery.getSuggestedReply();


                    emailService.sendAutoReply(fromEmail, subject, aiReply);


                    inbox.setFlags(new Message[]{message}, new Flags(Flags.Flag.SEEN), true);

                } catch (Exception e) {
                    System.err.println("Error processing a single email: " + e.getMessage());
                }
            }

            inbox.close(true);
            store.close();

        } catch (Exception e) {
            System.err.println("Error while polling for emails: " + e.getMessage());
        }
    }


    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";


        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        }

        else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);


                if (bodyPart.isMimeType("text/plain")) {
                    result = bodyPart.getContent().toString();
                    break; // Stop once we find the plain text
                }

                else if (bodyPart.isMimeType("text/html")) {
                    String htmlContent = bodyPart.getContent().toString();
                    result = htmlContent.replaceAll("<[^>]*>", "");// Will contain HTML tags
                }
            }
        }

        // Fallback
        if (result.isEmpty()) {
            result = message.getSubject();
        }

        return result;
    }
}