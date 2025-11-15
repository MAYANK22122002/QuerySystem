//package com.rapidquest.querysystem;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import jakarta.mail.internet.InternetAddress;
//
//// These imports are for reading email
//import jakarta.mail.*;
//import jakarta.mail.search.FlagTerm;
//import java.util.Properties;
//
//@Service
//public class EmailPollingService {
//
//    @Autowired
//    private QueryService queryService; // Inject your existing service
//    @Autowired
//    private EmailService emailService;
//    // These values come from application.properties
//    @Value("${spring.mail.username}")
//    private String username;
//
//    @Value("${spring.mail.password}")
//    private String password;
//
//    @Value("${spring.mail.imap.host}")
//    private String host;
//
//    /**
//     * This method runs every 60 seconds (60000 milliseconds)
//     */
//    @Scheduled(fixedRate = 60000)
//    public void pollForEmails() {
//        System.out.println("Checking for new emails...");
//        try {
//            Properties properties = new Properties();
//            properties.put("mail.store.protocol", "imaps"); // Use secure IMAP
//
//            Session session = Session.getInstance(properties, null);
//            Store store = session.getStore("imaps");
//
//            // Connect to Gmail using your App Password
//            store.connect(host, username, password);
//
//            // Open the "INBOX" folder in read-write mode
//            Folder inbox = store.getFolder("INBOX");
//            inbox.open(Folder.READ_WRITE);
//
//            // Search for all messages that are NOT "SEEN" (i.e., unread)
//            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
//
//            if (messages.length == 0) {
//                System.out.println("No new emails found.");
//                inbox.close(false);
//                store.close();
//                return;
//            }
//
//            // Loop through each new email
//            for (Message message : messages) {
//                try {
//                    String subject = message.getSubject();
//                    // Get the sender's email address
//                    String fromEmail = ((InternetAddress) message.getFrom()[0]).getAddress();
//
//                    System.out.println("New email found from: " + fromEmail);
//
//                    // --- THIS IS THE FULL-AUTO-REPLY LOGIC ---
//
//                    // 1. Create the query (this also generates the AI reply)
//                    Query newQuery = queryService.createNewQuery(subject, "Email", "Unassigned");
//
//                    // 2. Get the reply that was just generated
//                    String aiReply = newQuery.getSuggestedReply();
//
//                    // 3. Send the auto-reply back to the user
//                    emailService.sendAutoReply(fromEmail, subject, aiReply);
//
//                    // 4. Mark as read
//                    inbox.setFlags(new Message[]{message}, new Flags(Flags.Flag.SEEN), true);
//
//                } catch (Exception e) {
//                    System.err.println("Error processing a single email: " + e.getMessage());
//                    // Don't mark as read if processing fails, so we can retry
//                }
//            }
//
//            inbox.close(true);
//            store.close();
//
//        } catch (Exception e) {
//            System.err.println("Error while polling for emails: " + e.getMessage());
//        }
//    }
//}

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

                    // --- THIS IS THE UPGRADE ---
                    // We now call our new helper method to get the email body
                    String emailContent = getTextFromMessage(message);
                    // --------------------------

                    System.out.println("New email found from: " + fromEmail + " Subject: " + subject);

                    // 1. Create the query using the FULL email content
                    Query newQuery = queryService.createNewQuery(emailContent, "Email", "Unassigned");

                    // 2. Get the AI reply that was generated
                    String aiReply = newQuery.getSuggestedReply();

                    // 3. Send the auto-reply back to the user
                    emailService.sendAutoReply(fromEmail, subject, aiReply);

                    // 4. Mark as read
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

    /**
     * A helper method to intelligently extract plain text from an email message.
     * It handles multipart messages (text + HTML) and defaults to the subject.
     */
    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";

        // Check if the content is plain text
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        }
        // Check if it's a multipart message (e.g., text + HTML)
        else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                // Find the plain text part
                if (bodyPart.isMimeType("text/plain")) {
                    result = bodyPart.getContent().toString();
                    break; // Stop once we find the plain text
                }
                // If no plain text, take the HTML part (and strip tags later, but for now this is ok)
                else if (bodyPart.isMimeType("text/html")) {
                    String htmlContent = bodyPart.getContent().toString();
                    result = htmlContent.replaceAll("<[^>]*>", "");// Will contain HTML tags
                }
            }
        }

        // Fallback: If the body is empty or unreadable, use the subject
        if (result.isEmpty()) {
            result = message.getSubject();
        }

        return result;
    }
}