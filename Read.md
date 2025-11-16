# AI-Powered Audience Query Management System

**Submission for the RapidQuest Hackathon (November 2025) | by Mayank Tiwari**

[![Live Demo](https://img.shields.io/badge/Live_Demo-View_Here-brightgreen?style=for-the-badge)](https://querysystem.onrender.com)

This project is a complete, production-ready web application built to solve the hackathon's "Audience Query Management" challenge. It centralizes all incoming messages, uses AI to automatically analyze and route them, and even sends automated AI-generated troubleshooting replies.

<img width="1911" height="873" alt="image" src="https://github.com/user-attachments/assets/a1e45e87-6a33-4e8b-860f-a164fff56e06" />


---

## 🔗 Live Demo URLs

* **Unified Inbox:** [https://querysystem.onrender.com](https://querysystem.onrender.com)
* **Social Media Sim:** [https://querysystem.onrender.com/social_sim.html](https://querysystem.onrender.com/social_sim.html)
* **Chat Sim:** [https://querysystem.onrender.com/chat_sim.html](https://querysystem.onrender.com/chat_sim.html)
* **Analytics Dashboard:** [https://querysystem.onrender.com/analytics.html](https://querysystem.onrender.com/analytics.html)

---

## 📹 Demo Video

*[**Link to My 5-Minute Demo Video**](https://www.example.com) (Please add your video link here!)*

---

## 🚀 Core Features

* **Real-Time Email Ingestion:** A Spring Boot `@Scheduled` service (`EmailPollingService`) actively polls a live Gmail inbox (via IMAP) every 60 seconds to automatically ingest new queries as they arrive.
* **Multi-Channel Simulation:** Realistic demo pages for 'Social Media' and 'Chat' that post directly to the same backend, simulating a true multi-channel environment.
* **Efficient AI Triage (Single-Call):** Uses the Google Gemini API for a single, efficient analysis to determine three key data points at once:
    1.  **Category:** (Complaint, Question, Feature Request)
    2.  **Priority:** (High, Medium, Low)
    3.  **AI-Generated Reply:** A complete, empathetic troubleshooting response.
* **Fully Automated Workflows:**
    * **Escalation:** 'High' priority queries are automatically assigned to the "Tech Team," and an urgent alert is emailed to managers via the SendGrid API.
    * **Auto-Reply:** Queries from **Email** (via the poller) and **Chat** (via the sim) *automatically* send the AI-generated reply back to the user's email address, closing the loop instantly.
* **Live Analytics Dashboard:** A separate dashboard page (`analytics.html`) built with Chart.js that visualizes live metrics from the database.
* **Response Time Tracking:** Automatically calculates and displays the 'Average Response Time' (in minutes) by tracking when a ticket is created (`createdAt`) and when it's marked 'Resolved' (`resolvedAt`).

---

## 🧠 Approach & Key Decisions



### 1. Key Decision: SendGrid API over SMTP for Email
* **Problem:** After deploying to Render, all email sending failed with `Connection timed out`. Free-tier platforms like Render **block all outbound SMTP ports (587, 465)** to prevent spam.
* **Approach:** I pivoted the entire email-sending infrastructure. I removed the standard `spring-boot-starter-mail` (which relies on SMTP) and re-implemented `EmailService` using the **SendGrid API**.
* **Benefit:** By sending email over `HTTPS` (a standard web request, just like the Gemini API), I successfully bypassed the platform's firewall and fully implemented the automated email features.

### 2. Key Decision: Single-Call AI Triage
* **Problem:** The system needed three distinct pieces of data from the AI (category, priority, and a suggested reply). Making three separate API calls for every new query would be slow, expensive, and error-prone.
* **Approach:** I engineered a **single, complex prompt** in `QueryService` that instructs the AI to return one unified JSON object containing all three data points.
* **Benefit:** This single-shot analysis cuts API latency and cost by 66% and makes the application far more efficient and robust.

### 3. Key Decision: Multi-Stage Docker Build
* **Problem:** A simple `Dockerfile` would require manually running `mvn clean install` before building the image, and the final image would be bloated with the entire JDK and Maven cache.
* **Approach:** I implemented a **multi-stage `Dockerfile`**.
    1.  **Stage 1 (Builder):** Uses a full `maven:3-openjdk-17` image to build the project and create the `.jar` file.
    2.  **Stage 2 (Runner):** Uses a lightweight `amazoncorretto:17-al2-jdk` image.
* **Benefit:** The final image is small, secure (contains no source code), and builds reliably on Render.

### 4. Key Decision: Full Externalization of Secrets
* **Problem:** The app requires 8+ secrets (database credentials, API keys, email passwords). Committing these to GitHub is a massive security risk.
* **Approach:** The `application.properties` file is 100% clean and contains only **placeholders** (e.g., `${GEMINI_API_KEY}`). All real secrets are read from **environment variables** on the Render platform.
* **Benefit:** This is a production-grade security practice that allows the app to be deployed anywhere without code changes.

---

## 🛠️ Technical Architecture


* **Backend:** **Java 17**, **Spring Boot 3** (using Spring Web, Spring Data JPA, WebFlux for `WebClient`).
* **Frontend:** HTML5, CSS3, Vanilla JavaScript (ES6+).
* **Database:** **PostgreSQL** (Live on Render) / **H2** (Local Development).
* **AI:** **Google Gemini API** (via `WebClient` for non-blocking I/O).
* **Emailing (Ingestion):** **JavaMail (IMAP)** for polling the inbox.
* **Emailing (Sending):** **SendGrid API** (via `sendgrid-java` library).
* **Deployment:** **Docker** & **Render**.

---

## 💻 Setup Instructions (How to Run Locally)

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/MAYANK22122002/QuerySystem.git](https://github.com/MAYANK22122002/QuerySystem.git)
    cd QuerySystem
    ```

2.  **Set up Environment Variables (CRITICAL):**
    This project will not run without its environment variables. In IntelliJ, go to **Run > Edit Configurations...** and add the following 8 variables.

    *(Note: These are configured for the local H2 in-memory database, which is perfect for testing.)*

| Key | Value (Example) |
| :--- | :--- |
| `spring.datasource.url` | `jdbc:h2:mem:testdb` |
| `spring.datasource.username` | `sa` |
| `spring.datasource.password` | |
| `spring.jpa.database-platform` | `org.hibernate.dialect.H2Dialect` |
| `spring.h2.console.enabled` | `true` |
| `spring.h2.console.path` | `/h2-console` |
| `GEMINI_API_KEY` | `AIzaSy...` (Your Google Gemini API Key) |
| `spring.mail.username` | `your-hackathon-account@gmail.com` |
| `spring.mail.password` | `your-16-digit-gmail-app-password` |
| `escalation.email.to` | `your-personal-email@gmail.com` |
| `SENDGRID_API_KEY` | `SG.xxx...` (Your SendGrid API Key) |

3.  **Run the application:**
    Run the `QuerySystemApplication.java` file from your IDE.

4.  **Access the application:**
    * **Inbox:** `http://localhost:8080`
    * **Social Sim:** `http://localhost:8080/social_sim.html`
    * **Chat Sim:** `http://localhost:8080/chat_sim.html`
    * **Analytics:** `http://localhost:8080/analytics.html`
    * **H2 Database:** `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`)
