
# 💳 E-Wallet Simulator

A **Full-Stack E-Wallet application** implementing **secure digital wallet operations** with **ACID-compliant transactions**, **real-time balance updates**, **audit logging**, and a **modern responsive UI**.

---

## 📌 Features Overview

### 🔐 Authentication & Security

* User registration with **email & password validation**
* Secure login using **JWT authentication**
* Passwords encrypted using **BCrypt**
* Protected routes (frontend + backend)

---

### 💼 Wallet Management

* Automatic wallet creation on user registration
* Minimum initial balance enforcement
* Fetch wallet balance securely
* Persistent balance storage (MySQL)

---

### 💸 Money Transfer

* Transfer money between wallets
* Prevents:

  * Self-transfer
  * Negative transfers
  * Insufficient balance
* Fully **ACID-compliant** using Spring `@Transactional`

---

### ⚡ Real-Time Balance Updates

* **WebSocket integration**
* Sender and receiver balances update instantly without page refresh

---

### 📜 Transaction History

* Debit / Credit transactions clearly marked
* Color-coded UI (🔴 Debit | 🟢 Credit)
* “You” vs Counterparty wallet display
* Sorted by latest transaction

---

### 🧾 Audit Trail (Compliance-Ready)

* Logs critical user actions:

  * LOGIN (SUCCESS / FAILURE)
  * TRANSFER (SUCCESS / FAILURE)
  * RECEIVE
* Stores:

  * User ID
  * Old balance
  * New balance
  * Timestamp
* Audit logging runs in **independent transactions** (fail-safe)

---

### 🧪 Robust Testing & Quality Assurance
* **Unit Testing with Mockito**: Dependencies are mocked to test service logic in isolation, ensuring fast and reliable test execution.
* **Code Coverage with JaCoCo**: Integrated JaCoCo to track and maintain high testing standards.
* Comprehensive test cases for:
  * Context loading
  * Transaction safety & ACID properties
  * Validation & Exception handling

---

## 🧠 ACID Properties Demonstration

| Property        | How It Is Achieved                     |
| --------------- | -------------------------------------- |
| **Atomicity**   | Transfers rollback entirely on failure |
| **Consistency** | Balance invariants preserved           |
| **Isolation**   | Concurrent transfers don’t interfere   |
| **Durability**  | Data persists after server restart     |

---

## 🛠️ Tech Stack

### Backend

* Java 21
* Spring Boot
* Spring Security + JWT
* Spring Data JPA (Hibernate)
* MySQL (Dockerized)
* WebSockets
* Lombok
* JUnit 5 & Mockito (Testing & Mocking)
* JaCoCo (Code Coverage Tooling)

### Frontend

* React.js
* Axios
* React Router
* WebSockets
* CSS (Custom UI)

### DevOps / Tools

* Docker (MySQL only)
* Gradle
* Swagger UI
* Git & GitHub

---

## 📂 Project Structure

```text
ewallet_simulator/
├── backend/
│   ├── src/
│   │   ├── main/java/com/ewallet/wallet_service/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   │   └── impl/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   ├── dto/
│   │   │   ├── websocket/
│   │   │   ├── security/
│   │   │   └── exception/
│   │   └── test/java/com/ewallet/wallet_service/
│   ├── Dockerfile
│   ├── build.gradle
│   └── application.properties
│
├── frontend/
│   ├── src/
│   │   ├── pages/
│   │   ├── components/
│   │   ├── auth/
│   │   ├── websocket/
│   │   └── api/
│   ├── Dockerfile
│   └── package.json
│
├── Screenshots/
├── docker-compose.yml
├── video_overview.mp4
└── README.md


---

## 🐳 Running the Application (Docker)

▶️ One-Command Startup : docker compose up -d

---

## 🚀 Running the Application

### 1️⃣ Backend

* API: [http://localhost:8080]
* Swagger UI: [http://localhost:8080/swagger-ui.html]

---

### 2️⃣ Frontend

* App: [http://localhost:5173]

---

## 🧪 Testing & Quality Reports

* Run Unit Tests:

  cd backend
  ./gradlew clean test

* Generate JaCoCo Coverage Report:

  ./gradlew jacocoTestReport


### 📊 Test Report

* View Reports in Browser:

  * Test Summary: backend/build/reports/tests/test/index.html

  * Coverage Detail: backend/build/reports/jacoco/test/html/index.html


---

## 🧪 ACID Test Cases Summary

### Atomicity

* Transfer fails → balances unchanged

### Consistency

* Total wallet balance conserved

### Isolation

* Concurrent transfers safe

### Durability

* Restart server → data intact

---

## 📸 Screenshots Included

* Real-time balance updates
* Debit/Credit transactions
* Audit logs
* Validation errors
* JUnit test reports

---

## 👥 Contributors

* **Shravani Korde**
* **Gautam Jha**


---

## ✅ Conclusion

This project demonstrates **enterprise-grade transactional integrity**, **real-time systems**, and **secure financial application design**, making it suitable for **academic evaluation, internships, and interviews**.

---

