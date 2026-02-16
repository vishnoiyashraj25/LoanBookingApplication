# @Transactional in Loan Booking Application – Review Q&A

This document prepares you for project review questions about how you use **Spring’s `@Transactional`** for database consistency in this application.

---

## 1. What is @Transactional and why did you use it?

**Answer:**  
`@Transactional` is a Spring annotation that runs a method inside a **database transaction**. Spring starts a transaction before the method and either **commits** when the method finishes successfully or **rolls back** if an unchecked exception is thrown.

We use it so that **multi-step database operations either all succeed or all fail**. Without it, if one step passes and a later step fails (or the app crashes), the database can be left in an inconsistent state. With `@Transactional`, on any failure, all changes in that method are rolled back.

---

## 2. How is transactional support enabled in your project?

**Answer:**  
We use **Spring Boot** with **spring-boot-starter-data-jpa**. Spring Boot auto-configuration enables **declarative transaction management** by default when a `PlatformTransactionManager` (e.g. from JPA) is present. So we don’t need to add `@EnableTransactionManagement` explicitly—it’s already on.

We use **`@Transactional`** on the **service layer** (not on controllers or repositories) because:

- **Services** contain the business logic and coordinate multiple repository calls; that’s where transaction boundaries make sense.
- **Repositories** (e.g. Spring Data JPA) already perform single DB operations per call; we need transactions where we combine several of those.
- **Controllers** should stay thin and not define transaction boundaries.

---

## 3. Where have you used @Transactional and why in each case?

**Answer:**  
We use `@Transactional` in these service methods:

| Service | Method | Why transactional? |
|--------|--------|--------------------|
| **LoanRequestService** | `createLoanRequest` | Multiple DB operations: validate user, check in-process, optionally **reject previous request** and **cancel its pending applications**, then **save new LoanRequest**. All must commit or all roll back so we never have a new request with old ones left in INITIATED. |
| **LoanApplicationService** | `selectOffer` | Creates and saves a **LoanApplication** in one logical step. Keeps the “create application from request + offer” as a single unit. |
| **LoanApplicationService** | `updateStatus` | Updates **multiple LoanApplications** (cancel other pending), updates **LoanRequest** status, then Kafka event. All DB updates must be atomic so we don’t have “request in progress” with some applications not cancelled. |
| **LoanApplicationService** | `processApprovedApplication` | Called after approval (e.g. from Kafka consumer). Creates **Loan**, **EMI schedules**, and updates **LoanRequest** and **LoanApplication** status. Either the full set commits or, on exception, we set REJECTED and save in `finally`; the transaction still ensures no partial Loan/EMI data. |
| **LoanService** | `createLoan` | Saves **Loan** and then creates **all EMI schedules** via `EmiService.createEMI`. Loan and EMIs must be created together; no “loan without EMIs” or “EMIs without loan”. |
| **PaymentService** | `createPayment` | Updates **EmiSchedule** (mark PAID), updates **Loan** (dues amount and possibly status CLOSED), and saves **Payment**. Payment, EMI, and loan state must stay in sync. |

---

## 4. How does a transaction “roll back” in your code? When does it happen?

**Answer:**  
With default settings, Spring **rolls back** the transaction when the transactional method throws an **unchecked exception** (e.g. `RuntimeException`, `ResourceNotFoundException`, `ConflictException`). All DB changes made within that method (and in other methods that joined the same transaction) are reverted.

**Examples in our app:**

- In **createLoanRequest**, if we throw `ConflictException` (“loan request already in progress”), nothing is committed—no previous request rejected, no new request saved.
- In **createPayment**, if “Loan not found” or “No pending EMI” throws `ResourceNotFoundException`, no EMI or Loan or Payment rows are updated/saved.
- In **processApprovedApplication**, if `loanService.processApprovedLoan(application)` throws (e.g. account not found), the transaction rolls back so we don’t have a Loan without EMIs. In the `catch` block we set status to REJECTED and save in `finally`, so when we don’t rethrow, the transaction commits with REJECTED state instead of partial data.

---

## 5. What is transaction propagation and what do you use?

**Answer:**  
**Propagation** defines how a new transactional method behaves when it’s called from another transactional method. We use the **default: `REQUIRED`**.

- **REQUIRED (default):** If the caller already has a transaction, the called method **joins that same transaction**. If the caller has no transaction, a new one is created.

**Example in our project:**  
`LoanApplicationService.processApprovedApplication` is `@Transactional`. It calls `loanService.processApprovedLoan(application)`, which in turn calls `createLoan(application)`. `createLoan` is also `@Transactional`. With REQUIRED, `createLoan` does **not** start a new transaction; it runs in the same transaction as `processApprovedApplication`. So saving the Loan and all EMIs is in one transaction with the later updates to LoanRequest and LoanApplication. One failure rolls back everything.

We didn’t need to set `propagation` explicitly; default REQUIRED is what we want for these flows.

---

## 6. Why not put @Transactional on the controller or repository?

**Answer:**

- **Controller:** Controllers should handle HTTP and delegate to services. Putting `@Transactional` there would tie transaction boundaries to web layer and could keep transactions open for the whole request (e.g. during serialization), which is bad for connection holding and clarity. Service layer is the right place for “this use case is one transaction.”
- **Repository:** Spring Data JPA repositories already run each method in a transaction (read-only for queries when applicable). Our need is to group **multiple** repository calls (e.g. save loan + save many EMIs) into one transaction. That grouping is business logic, so it belongs in the service.

---

## 7. How does @Transactional work under the hood (briefly)?

**Answer:**  
Spring uses **AOP (Aspect-Oriented Programming)**. When you call a `@Transactional` method:

1. A **proxy** around the service is invoked (not the raw object).
2. The proxy gets a **database connection** and sets **autoCommit = false**, then starts the transaction.
3. Your method runs; all repository calls in that thread use the **same connection** (same transaction).
4. If the method **returns normally**, the proxy **commits** the transaction.
5. If the method throws an **unchecked exception**, the proxy **rolls back** the transaction.
6. The proxy then releases the connection back to the pool.

That’s why **self-invocation** (one method in the same class calling another `@Transactional` method) doesn’t start a new transaction: the inner call doesn’t go through the proxy. In our project we don’t rely on self-invocation for new transactions; when we need a transaction we call through the proxy (e.g. controller → service, or one service → another service).

---

## 8. Did you use rollbackFor, noRollbackFor, or readOnly? Why or why not?

**Answer:**  
We use the **default** behaviour:

- **Rollback:** We throw **unchecked** exceptions (`ResourceNotFoundException`, `ConflictException`, `BadRequestException`, etc.). By default Spring rolls back on `RuntimeException` and subclasses, so we get rollback without specifying `rollbackFor`. If we had used only checked exceptions for failures, we would add `rollbackFor = SomeCheckedException.class`.
- **readOnly:** We didn’t set `readOnly = true` on any method. Our transactional methods are **write** operations (create/update). For pure read-only use cases (e.g. a service method that only runs queries), we could add `@Transactional(readOnly = true)` for optimizations (e.g. no flush, possible DB optimizations); we can mention that as a possible improvement.
- **noRollbackFor:** We didn’t need it; we want rollback on any failure in those methods.

---

## 9. How does processApprovedApplication use transactions with Kafka?

**Answer:**  
`processApprovedApplication` is called from the **Kafka consumer** after a loan is approved. It is `@Transactional` and:

1. Loads **LoanApplication** and **LoanRequest**.
2. Calls **loanService.processApprovedLoan(application)** → which creates **Loan** and **all EMIs** in the **same transaction** (REQUIRED).
3. In **try**: sets request status to COMPLETED and application to APPROVED.
4. In **catch**: sets request and application to REJECTED and stores error message.
5. In **finally**: saves **LoanRequest** and **LoanApplication**.

The **transaction** ensures that either the full set (Loan + EMIs + status updates) is committed, or on exception we don’t leave a Loan with missing EMIs—we catch, set REJECTED, and commit that state. Kafka consumption is **outside** the transaction; we only use the transaction for DB consistency. We don’t send Kafka messages inside the transactional method for the “approved” path; the event was sent earlier in `updateStatus`.

---

## 10. What would happen if you removed @Transactional from createLoanRequest?

**Answer:**  
Each repository call would run in its **own** transaction (or a very short one per call). So we could end up with:

- Previous request marked REJECTED and its applications CANCELLED, but then a failure before saving the new request → **inconsistent**: old request is already updated, new one doesn’t exist.
- Or new request saved, but a failure had already happened when updating previous applications → **inconsistent**: two INITIATED requests or wrong statuses.

With `@Transactional`, any exception causes **rollback of all** those changes, so we never leave the DB in a half-updated state.

---

## Quick reference – Where @Transactional is used

| File | Method |
|------|--------|
| `LoanRequestService` | `createLoanRequest` |
| `LoanApplicationService` | `selectOffer`, `updateStatus`, `processApprovedApplication` |
| `LoanService` | `createLoan` |
| `PaymentService` | `createPayment` |

All use **default propagation (REQUIRED)** and **default rollback** (rollback on unchecked exceptions). No explicit `readOnly`, `rollbackFor`, or `noRollbackFor` in the codebase.
