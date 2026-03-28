<div align="center">
  <h1>🎓 AI Mock Interview & Mentor Platform</h1>
  <p>A scalable microservices-based platform for seamless mock interviews, targeting IT & Marketing Freshers/Interns.</p>
  <p>
    <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
    <img src="https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white" />
    <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" />
    <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" />
    <img src="https://img.shields.io/badge/Gemini_LLM-8E75B2?style=for-the-badge&logo=google&logoColor=white" />
    <img src="https://img.shields.io/badge/Cloudflare-F38020?style=for-the-badge&logo=cloudflare&logoColor=white" />
    <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
  </p>
</div>

## 📖 Overview

A comprehensive platform assisting students, Freshers, and Interns in honing their interview skills through a professional Mentor network and a personalized AI Mock Interview model. Operating on a distributed Microservices architecture, the system's core AI workflow strictly follows 3 phases: **Setup** (parsing CV/JD to create a Blueprint), **Run** (executing the interview), and **Report** (evaluating and grading). The architectural highlight is its clear separation of concerns: **Java Spring Boot hard-codes and manages the entire state machine loop**, while **LLM (Python) combined with PostgreSQL/Redis specializes in generating foundational questions** (e.g., basic algorithms, OOP, 4P Marketing) and analyzing responses.

## 💻 Tech Stack

* **Core Backend:** Java Spring Boot (Business Logic Management, State Machine for Chat Orchestrator, API Gateway).
* **AI Microservices:** Python - Acts as the bridge for AI processing and LLM communication.
    * **FastAPI:** Builds high-performance API endpoints for analyzing, orchestrating, and generating reports.
    * **Pydantic:** Strictly manages and validates Data Transfer Objects (DTOs) to synchronize with the Core Backend.
    * **Google GenAI SDK:** Directly integrates Gemini and Gemma models for text processing, answer evaluation, and learning path generation.
* **Database & Caching:**
    * **PostgreSQL:** Stores system data and utilizes `pgvector` for Meta-driven RAG semantic retrieval.
    * **Redis:** High-speed data caching and Session State management for the system's Pause/Resume functionality.
* **Payment & Integration:** PayOS (Processing e-wallet top-up transactions).

## 🖥️ Server & Infrastructure

The system is deployed on a highly optimized Homelab environment, running 24/7 with a centralized container management architecture:

* **Hardware & OS:** Bare-metal server operating on Ubuntu Desktop LTS.
* **Networking & Tunneling:** Zero-Trust Network architecture using Cloudflare Tunnel (HTTP/HTTPS) and TCP Tunneling to safely expose services to the public without opening direct modem ports.
* **Container Management:** Fully operated via Docker Native with a centralized stack manager (Dockge).
* **Monitoring & Observability:** Integrated Real-time Tracing Viewer with cross-layer `traceId` for easy request flow tracking, combined with an Agent continuously monitoring system resources (CPU/RAM/Storage).

## ⚙️ System Architecture & Flow

The system is optimized into 3 AI processing phases to ensure accuracy and strict adherence to the interview script:

1.  **Setup Phase (Initialization):** LLM parses user-uploaded CVs and JDs, enhancing the data to generate a personalized "Blueprint" (JSON script) with appropriate topics for Freshers.
2.  **Run Phase (Chat Orchestrator):** The Core Backend coordinates the interview flow based on the Blueprint, utilizing Redis to store real-time session states. LLM analyzes the candidate's answers; if insufficient, it returns a `followup` tag to dig deeper. State management and the Pause/Resume feature are 100% controlled by Java Spring Boot.
3.  **Report Phase (Evaluation):** Applies Meta-driven RAG (via PostgreSQL `pgvector`) to cross-reference answers against standard knowledge bases. The LLM uses this context to provide scores (Grading) and detailed feedback for each question.

## ✨ Core Features

### 1. Personalized AI Mock Interview
* **Smart Parsing:** Automatically extracts profiles from CVs and standardizes JDs to create realistic scenarios.
* **Contextual Interaction:** Two-way Voice/Text interaction with highly optimized LLM prompt wrappers.
* **Dynamic State Machine:** Pause and Resume the interview without losing context, powered by Redis; AI dynamically drills down using the `followup` tag mechanism.

### 2. 1-1 Human Mentoring
* **Booking & In-App Room:** Flexible scheduling and direct interviewing via a built-in meeting room.
* **2-Way Feedback:** Mentors evaluate candidate expertise; candidates rate the Mentor's interview quality.
* **Admin Management:** Comprehensive dashboard to monitor Mentor metrics and quality.

### 3. Dynamic Learning Path
* **Personalized Roadmap:** Automatically generates a 7-14-21 day study roadmap based on AI Report results, focusing on the Fresher's weak areas.
* **Daily Quizzes:** Daily multiple-choice exercises with automated grading and in-depth explanations.

### 4. Community & New Features
* **Knowledge Newsfeed:** A mini social network for sharing articles, interacting, and commenting on interview experiences.
* **Direct In-App Chat:** Allows all users (Admin, User, Mentor) to message each other directly on the platform.
* **Automated Notifications & Reminders:** System automatically sends in-app notifications and reminder emails for mentor appointments, billing invoices, and important events.

### 5. Billing
* **In-App Wallet:** Integrated with PayOS for topping up balances and purchasing interview packages or premium services.

### 6. Admin Dashboard (System Management)
* **User & Mentor Management:** Manages user profiles and executes Mentor approval/rejection workflows based on strict professional standards.
* **Content & Session Moderation:** Moderates Knowledge Newsfeed posts and comprehensively monitors the quality of Mock Interview sessions.
* **Financial & Package Control:** Tracks revenue, reconciles PayOS transactions, and flexibly creates/configures subscription packages.
* **System Broadcasting:** Manages campaigns and proactively sends system-wide announcements (push notifications, emails) to specific user segments.
