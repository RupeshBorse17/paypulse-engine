# 🚀 PayPulse Engine — High-Concurrency Settlement Core

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15%2B-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/Architecture-Idempotency%20%26%20Concurrency-red.svg)]()

**PayPulse Engine** is a robust, fail-safe, and high-concurrency financial transaction settlement system built using **Spring Boot 3** and **PostgreSQL**. 

It is engineered to handle inter-account fund transfers safely, preventing double-deductions and data corruption caused by network delays, double-clicks, or simultaneous race conditions.

---

## 🎯 Key Engineering Highlights

* **🔒 Idempotent API Design (`Idempotency-Key`):** Prevents duplicate financial transfers caused by user double-clicks or client-side retries. Every transaction request is tagged with a unique key.
* **⚡ Concurrency Control (Optimistic Locking):** Leverages JPA `@Version` annotation to solve the **Lost Update Problem**. Prevents corrupt account balances when multiple transactions target the same account within the exact same millisecond.
* **🗄️ PostgreSQL Native Mapping:** Seamlessly maps PostgreSQL custom `ENUM` data types (`SUCCESS`, `FAILED`, `PENDING`) to Java domain entities using Hibernate's `@ColumnTransformer`.
* **📑 Complete Audit Trailing:** Ensures 100% financial traceability by logging every state transition of a settlement attempt into dedicated audit logs.

---

## 🏗️ Core Fund Transfer Workflow

```text
[Client App / Postman]
         │ (POST /api/v1/settlements with Idempotency-Key)
         ▼
[PayPulse Engine API]
         │
         ├───► 1. Check Idempotency Key ───► (If processed: Return Cached Response)
         │
         ├───► 2. Fetch Account & Check Version (@Version = 1)
         │
         ├───► 3. Execute Debit & Credit Logic ($1000 Sender ──► Receiver)
         │
         └───► 4. Commit to DB ───► (Updates Balance & Version: 1 -> 2)
                                   └─► If Version Conflict (OptimisticLockException)
                                       └──► Abort / Trigger Retry Flow
