# Enterprise AI Research Platform (SaaS Edition)

An Event-Driven, Multi-Tenant Microservices platform that takes a user query, searches the web, reads multiple websites, and uses a local Large Language Model (via LM Studio) to synthesize a comprehensive research report with real-time updates and Redis caching.

## Architecture

This project is built on a scalable, Polyglot Microservices architecture:
* **API Gateway & Core Service**: Java 17, Spring Boot, Spring Security (Stateless JWT)
* **Real-Time Communication**: WebSockets (STOMP + SockJS)
* **High-Performance Caching**: Redis (24h AI Report Caching)
* **Message Broker**: RabbitMQ
* **AI Worker**: Python (Pydantic, Pika, Tavily, Jina AI)
* **Database**: PostgreSQL (User accounts & Research Tasks)
* **LLM Provider**: LM Studio (Local LLMs)
* **Frontend**: Vue 3 (Composition API), Vue Router 4, TypeScript, Tailwind CSS v4, StompJS

---

## Infrastructure Setup

1. **Start Infrastructure (Docker)**
   ```bash
   docker compose up -d
   ```
   This spins up:
   - **PostgreSQL**: Port `5433` (DB: `research_db`)
   - **RabbitMQ**: Port `5672` (Management UI: `http://localhost:15672`)
   - **Redis**: Port `6379` (In-memory caching)

2. **Start the Java API (Backend)**
   ```bash
   cd backend
   mvn clean spring-boot:run
   ```

3. **Start the Python AI Worker**
   ```bash
   cd ai-worker
   pip install -r requirements.txt
   python main.py --worker
   ```

4. **Start the Vue 3 Frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

5. **Environment Variables**
   Create a `.env` file in the root directory:
   ```env
   TAVILY_API_KEY="your_tavily_token"
   ```

---

## Authentication & API Usage

1. **Register a New Account**:
   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username": "danzevo", "password": "password123"}'
   ```

2. **Log in to get a JWT Token**:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "danzevo", "password": "password123"}'
   ```

3. **Submit an Authenticated Research Task**:
   ```bash
   curl -X POST http://localhost:8080/api/research \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
     -d '{"topic": "Quantum Computing in Drug Discovery"}'
   ```

---

## CI/CD Pipeline
This repository features a fully automated, Polyglot CI/CD pipeline using **GitHub Actions**. Every push to the `main` branch triggers a Matrix Strategy that spins up 3 parallel virtual machines to compile the Java, Python, and Node environments simultaneously into multi-stage Docker images on the GitHub Container Registry (GHCR).
