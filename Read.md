# AI-Powered Audience Query Management System

**Submission for the RapidQuest Hackathon (November 2025) | by Mayank Tiwari**

[![Live Demo](https://img.shields.io/badge/Live_Demo-View_Here-brightgreen?style=for-the-badge)](https://querysystem.onrender.com)

This project is a complete, deployable web application built to solve the challenge of managing high volumes of audience queries. It centralizes all incoming messages, uses AI to automatically analyze and route them, and even sends automated AI-generated troubleshooting replies.

![The Unified Inbox Dashboard](https://i.imgur.com/GZ5lG5c.png) 
*(**Note:** Please replace this link with a real screenshot of your `index.html` dashboard!)*

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

* **Real-Time Email Ingestion:** A Spring Boot `@Scheduled` service actively polls a live Gmail inbox (via IMAP) every 60 seconds to automatically ingest new queries as they arrive.
* **Multi-Channel Simulation:** Realistic demo pages for 'Social Media' and 'Chat' that post directly to the same backend, simulating a true multi-channel environment.
* **Efficient AI Triage (Single-Call):** Uses the Google Gemini API for a single, efficient analysis to determine three key data points at once:
    1.  **Category:** (Complaint, Question, Feature Request)
    2.  **Priority:** (High, Medium, Low)
    3.  **AI-Generated Reply:** A complete, empathetic troubleshooting response.
* **Fully Automated Workflows:**
    * **Escalation:** 'High' priority queries are automatically assigned to the "Tech Team," and an urgent alert is emailed to managers via the SendGrid API.
    * **Auto-Reply:** Queries from **Email** and **Chat** *automatically* send the AI-generated reply back to the user's email address, closing the loop instantly.
* **Live Analytics Dashboard:** A separate dashboard page built with Chart.js that visualizes live metrics from the database.
* **Response Time Tracking:** Automatically calculates and displays the 'Average Response Time' (in minutes) by tracking when a ticket is created (`createdAt`) and when it's marked 'Resolved' (`resolvedAt`).

---

## 🛠️ Technical Architecture

This project is a full-stack Java application built with a modern, decoupled, and scalable architecture.



* **Backend:** **Java 17**, **Spring Boot 3** (using Spring Web, Spring Data JPA, WebFlux for `WebClient`).
* **Frontend:** HTML5, CSS3, Vanilla JavaScript (ES6+).
* **Database:** **PostgreSQL** (Live on Render) / **H2** (Local Development).
* **AI:** **Google Gemini API** (via `WebClient` for non-blocking I/O).
* **Emailing (Ingestion):** **JavaMail (IMAP)** for polling the inbox.
* **Emailing (Sending):** **SendGrid API** (to bypass platform SMTP blocks).
* **Deployment:** **Docker** & **Render** (using a multi-stage `Dockerfile` for an optimized, secure build).

---

## 🧠 Key Decisions & Challenges

This project required overcoming several real-world engineering challenges.

### 1. Challenge: AI API Inefficiency
* **Problem:** The system needed three distinct pieces of data from the AI (category, priority, and a suggested reply). Making three separate API calls for every new query would be slow, expensive, and error-prone.
* **Decision:** I engineered a **single, complex prompt** that instructs the AI to return one unified JSON object containing all three data points. This single-shot analysis cuts API latency and cost by 66% and makes the application far more efficient.

### 2. Challenge: Blocked Email (SMTP) on Deployment
* **Problem:** After deploying to Render, the application crashed. The `Connection timed out` error revealed that the free-tier platform **blocks all outbound SMTP ports (like 587)** to prevent spam. This completely broke my email notification and auto-reply features.
* **Decision:** I pivoted the entire email-sending infrastructure. I removed the standard `spring-boot-starter-mail` (which relies on SMTP) and replaced it with the **SendGrid API**. By sending email over `HTTPS` (a standard web request), I successfully bypassed the platform's firewall and fully implemented the automated email features.

### 3. Challenge: Persistent Data & Secure Deployment
* **Problem:** The local H2 in-memory database would be erased every time the Render service restarted. Furthermore, all API keys and passwords needed to be secure.
* **Decision:** I migrated the project to a live, persistent **PostgreSQL** database. All 8+ secrets (database credentials, API keys, email passwords) are fully externalized and read from **environment variables** on the Render platform, following best security practices. The `application.properties` file in this repo is 100% clean of any secrets.

---

## 💻 How to Run Locally

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/MAYANK22122002/QuerySystem.git](https://github.com/MAYANK22122002/QuerySystem.git)
    cd QuerySystem
    ```

2.  **Set up Environment Variables (CRITICAL):**
    This project will not run without its environment variables. In IntelliJ, go to **Run > Edit Configurations...** and add the following variables.

    *(Note: These are for the H2 in-memory database, which is perfect for local testing.)*

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
