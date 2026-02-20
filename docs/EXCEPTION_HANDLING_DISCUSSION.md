# Exception Handling – Discussion Preparation

This document explains **how** exception handling is implemented in the Loan Booking Application, **why** certain choices were made, and **how to answer** common discussion or interview questions.

---

## 1. Overall Strategy

**In one line:** Controllers stay thin and never catch exceptions; the **service layer** throws **domain-specific exceptions**; a **global handler** (`@RestControllerAdvice`) turns those (and framework exceptions) into consistent HTTP responses.

| Layer | Responsibility |
|-------|----------------|
| **Controller** | No try-catch; just delegates to service. Any exception propagates. |
| **Service** | Throws `ResourceNotFoundException`, `ConflictException`, `BadRequestException`, or lets framework/Jackson exceptions propagate. |
| **GlobalExceptionHandler** | Catches all of the above and returns `ResponseEntity` with appropriate status and JSON body. |
| **Kafka consumer** | Treats `BusinessException` as “reject but don’t retry”; other exceptions trigger retry then DLT. |

**Why this design?**  
- Single place to define “exception → HTTP response” (no duplicated catch blocks in controllers).  
- Clear API contract: clients always get a structured error body.  
- Separation of concerns: business logic throws domain exceptions; the web layer only maps them to HTTP.

---

## 2. Custom Exception Hierarchy

### 2.1 Class Diagram (Conceptual)

```
RuntimeException
├── BadRequestException          (invalid input / bad params)
└── BusinessException
    ├── ResourceNotFoundException (entity not found → 404)
    └── ConflictException        (state conflict, duplicate → 409)
```

### 2.2 Why Extend `RuntimeException`?

- **Unchecked:** No need to declare `throws` in every service/controller.  
- **Intentional:** Business and validation failures are “expected” and handled by the global handler; we don’t want to force callers to catch them.  
- **Consistent with Spring:** Most Spring exceptions (e.g. `DataAccessException`) are also unchecked.

### 2.3 Why Does `ResourceNotFoundException` Extend `BusinessException`?

- **Kafka behavior:** In the loan-approved consumer, we want “not found” and “already processed” to be treated the same: **business rule failure → ack and don’t retry**.  
- So we have one parent type `BusinessException` that means “reject this message, no retry, no DLT.”  
- `ResourceNotFoundException` and `ConflictException` are both “business” outcomes, so they extend `BusinessException`.

### 2.4 Why Is `BadRequestException` Not Under `BusinessException`?

- **Semantics:** “Bad request” = invalid input (wrong enum, invalid JSON, validation failure).  
- **Usage:** Used for request/validation issues (e.g. invalid payment status in `PaymentService`).  
- **Kafka:** In our design, bad-request-style issues in the consumer could be argued either way; keeping `BadRequestException` separate keeps “business rule rejection” (ack, no retry) clearly tied to `BusinessException` hierarchy.

**When to use which?**

| Exception | When to use | HTTP status |
|-----------|-------------|-------------|
| `ResourceNotFoundException` | Entity not found by ID or filter (e.g. “Loan request not found”, “No users found”) | 404 |
| `ConflictException` | State conflict (e.g. “KYC already verified”, “Loan request already in progress”) | 409 |
| `BadRequestException` | Invalid parameter or payload (e.g. invalid enum value, invalid JSON) | 400 |

---

## 3. GlobalExceptionHandler – What Each Handler Does

The class is annotated with `@RestControllerAdvice`, so it applies to **all** `@RestController` (and similar) endpoints. Each method is annotated with `@ExceptionHandler(SomeException.class)`.

### 3.1 Validation Exceptions

**MethodArgumentNotValidException**  
- **When:** Request body fails `@Valid` (e.g. on `@RequestBody UserRequest request`).  
- **Why:** Spring triggers this when bean validation fails. We map field errors to a map `field → message` and return 400.  
- **Example:** Missing email, invalid size, etc. on DTOs.

**ConstraintViolationException**  
- **When:** Method parameters or return values fail validation (e.g. `@Validated` on controller + `@Positive` on `Long id`).  
- **Why:** This is for **method-level** validation (path variables, request params), not body. We return 400 with a `details` map of constraint violations.

**Difference in one line:**  
- `MethodArgumentNotValidException` → body validation (`@Valid` on body).  
- `ConstraintViolationException` → path/query/param validation (`@Validated` + `@Positive`, `@Size`, etc.).

### 3.2 Domain Exceptions (Our Custom Ones)

- **ResourceNotFoundException** → 404, body with `timestamp`, `status`, `error`, `message`.  
- **ConflictException** → 409, same structure.  
- **BadRequestException** → 400, same structure.

We use a consistent structure (timestamp, status, error, message) so clients can parse errors uniformly.

### 3.3 Framework / Library Exceptions

**HttpMessageNotReadableException**  
- **When:** Request body is not valid JSON or doesn’t match expected types (e.g. wrong type for an enum).  
- **Why we handle it specially:** We check if the cause is `InvalidFormatException` for an **enum** and return a helpful body with `field`, `invalidValue`, `allowedValues`.  
- **Else:** Generic “Invalid request payload” 400.

**JsonProcessingException**  
- **When:** Jackson fails during serialization/deserialization (e.g. in services that do `mapper.writeValueAsString` / `readValue`).  
- **Why:** We could let it propagate as 500; by handling it we return 400 with a clear “Invalid JSON” message, which is more accurate for client errors.

**IllegalArgumentException**  
- **When:** Invalid argument to a method (e.g. invalid enum from `PaymentStatus.valueOf(statusStr)` if we didn’t wrap it in `BadRequestException`).  
- **Why:** We map it to 400 so that invalid input doesn’t surface as 500.

**IllegalStateException**  
- **When:** Operation not allowed in current state.  
- **Why:** We map it to 409 (Conflict), as it usually represents a state conflict.

**DataIntegrityViolationException**  
- **When:** JPA/DB constraint violation (unique, foreign key, not-null).  
- **Why:** We don’t expose raw SQL/constraint names. We use `getMostSpecificCause()` and message content to return a **user-friendly** message (e.g. “Resource already exists”, “Referenced resource does not exist”) and 409.

**NoHandlerFoundException**  
- **When:** No controller method matches the request (e.g. wrong URL).  
- **Why:** We return 404 with method and path so the client knows the endpoint doesn’t exist.

### 3.4 RuntimeException (Broad Fallback)

- **When:** Any other `RuntimeException` not caught by a more specific handler.  
- **Why:** Prevents uncaught runtime exceptions from becoming a default Spring error page; we return a consistent JSON body with 400.  
- **Note:** More specific handlers (e.g. `ResourceNotFoundException`) are checked first because Spring matches the **most specific** exception type.

### 3.5 Order of Handlers

Spring selects the **most specific** matching handler. So:

- `ResourceNotFoundException` is handled by `handleResourceNotFound`, not by `handleRuntimeException`.  
- Same for `ConflictException`, `BadRequestException`, etc.  
- Only when no more specific handler matches does `RuntimeException` get used.

**Optional improvement:** Add a catch-all `@ExceptionHandler(Exception.class)` that returns 500 with a generic message. That way any **checked** or unexpected exception still returns JSON instead of an HTML error page.

---

## 4. Where We Throw What (Service Layer)

| Service | Exception | When |
|---------|-----------|------|
| **LoanApplicationService** | `ResourceNotFoundException` | Loan request / offer / application not found; no applications for filter. |
| | `ConflictException` | Loan request not active; application already processed; loan request not active; application already approved/rejected. |
| **LoanRequestService** | `ResourceNotFoundException` | User not found. |
| | `ConflictException` | Loan request already in progress. |
| **EligibleOfferService** | `ResourceNotFoundException` | Loan request not found. |
| | `ConflictException` | Loan request closed / already proceeded / already in progress. |
| **KYCService** | `ResourceNotFoundException` | User not found. |
| | `ConflictException` | KYC already verified. |
| **UserService, LenderService, OfferService** | `ResourceNotFoundException` | No users / lenders / offers (for given filters). |
| | (via try-catch) `BadRequestException` | JSON serialization failure in get-all methods. |
| **AccountService** | `ResourceNotFoundException` | User not found; account not found. |
| **LoanService** | `ResourceNotFoundException` | User account not found. |
| **PaymentService** | `ResourceNotFoundException` | Loan not found; no pending EMI. |
| | `BadRequestException` | Invalid `statusStr` (e.g. not a valid `PaymentStatus` enum). |
| **EmiService** | `ResourceNotFoundException` | No EMI for given criteria. |

**Pattern:**  
- **Not found** → `ResourceNotFoundException`.  
- **State / duplicate / already done** → `ConflictException`.  
- **Invalid input (enum, format)** → `BadRequestException` (or let framework throw and we handle e.g. `IllegalArgumentException` in the global handler).

---

## 5. Kafka: Why Exception Handling Is Different

### 5.1 Two Types of Failures

1. **Business rule failure**  
   Example: “Application not found”, “Application already approved”.  
   - Retrying won’t fix it.  
   - We want: **ack the message** (so we don’t reprocess) and **don’t send to DLT** (it’s not a poison message).

2. **Transient / technical failure**  
   Example: DB down, NPE, bug.  
   - Retrying might fix it.  
   - We want: **don’t ack** → Kafka retries → after max retries, send to DLT.

### 5.2 How We Achieve This

**KafkaConfig (DefaultErrorHandler):**  
- When the listener **does not ack**, the `DefaultErrorHandler` runs: **exponential backoff retries** (e.g. 3 retries, 2s / 4s / 8s).  
- After retries exhausted, **DeadLetterPublishingRecoverer** sends the record to `<topic>.DLT` and then the offset is committed so consumption can continue.

**LoanApplicationService.processApprovedApplication:**  
- For **business failures** inside `loanService.processApprovedLoan(application)`, we **catch `BusinessException`**: we set status to REJECTED, set error message, save, and **do not rethrow**. So the consumer sees success and **acks**.  
- For **any other exception** we don’t catch, so it **propagates** to the listener → listener doesn’t ack → retry then DLT.

**At the very start of processApprovedApplication:**  
- We throw `ResourceNotFoundException` or `ConflictException` (e.g. “Application not found”, “Application is already approved or rejected”) **before** the try block. Those **do** propagate to the consumer.  
- So the **consumer** must either:  
  - Catch `BusinessException` (and subclasses), then ack and optionally log; or  
  - Rely on the fact that after retries these will go to DLT (less ideal for “not found” / “already processed”).  
- **Recommended:** In the Kafka listener, catch `BusinessException`, log, and ack so that business rejections are not retried and not sent to DLT.

### 5.3 Summary Table

| Scenario | Who throws / catches | Consumer acks? | Retry? | DLT? |
|---------|---------------------|----------------|--------|------|
| Success | – | Yes | No | No |
| BusinessException (e.g. inside processApprovedLoan) | Caught in service, status set to REJECTED | Yes | No | No |
| ResourceNotFoundException / ConflictException at start of processApprovedApplication | Thrown by service, propagates to consumer | Only if consumer catches BusinessException | If no ack: yes | If no ack: after retries |
| Any other exception (e.g. NPE, DB error) | Propagates to consumer | No | Yes | Yes |

---

## 6. Validation: @Valid vs @Validated

- **@Valid:** On **request body** (and nested objects). Triggers **MethodArgumentNotValidException** on failure.  
- **@Validated:** Class-level (e.g. on controller). Enables **method parameter** validation (`@Positive`, `@Size`, etc.). Triggers **ConstraintViolationException** on failure.

We use both: `@Valid` on `@RequestBody` DTOs and `@Validated` on controllers with path/query constraints so that both body and params are validated and handled by our global handler.

---

## 7. PaymentService: Why Catch IllegalArgumentException and Throw BadRequestException?

```java
try {
    status = PaymentStatus.valueOf(statusStr.toUpperCase());
} catch (IllegalArgumentException e) {
    throw new BadRequestException(
        "Invalid payment status: " + statusStr + ". Allowed values: " + ...);
}
```

- **Reason:** We want a **clear, API-specific message** and **explicit 400** via `BadRequestException`.  
- If we didn’t catch, `IllegalArgumentException` would still be handled by the global handler and return 400, but the message would be generic. By wrapping, we include **allowed values** in the response, which is better for API consumers.

---

## 8. Possible Discussion / Interview Questions and Answers

**Q1: Why use @RestControllerAdvice for exception handling?**  
- One central place for “exception → HTTP response” for all controllers. No duplicated try-catch in controllers; consistent error format and status codes.

**Q2: Why are your custom exceptions unchecked (RuntimeException)?**  
- So we don’t have to declare `throws` everywhere. They represent “expected” failure cases that the global handler turns into HTTP errors. Spring’s own exceptions are mostly unchecked too.

**Q3: What is the difference between MethodArgumentNotValidException and ConstraintViolationException?**  
- First: request **body** validation (`@Valid` on body). Second: **method parameter** validation (`@Validated` + `@Positive`, `@Size` on path/query params).

**Q4: Why does ResourceNotFoundException extend BusinessException?**  
- So that in the Kafka consumer we can treat “not found” and “already processed” the same: business failure → ack, no retry, no DLT. One type (`BusinessException`) to catch for “reject and move on.”

**Q5: Why handle DataIntegrityViolationException and not expose the raw message?**  
- Raw messages often contain table/column names and SQL; not suitable for API clients. We map to short, safe messages (e.g. “Resource already exists”) and return 409.

**Q6: Why do you handle HttpMessageNotReadableException specially for enums?**  
- So the client gets `allowedValues` and the invalid value in the response, making it easy to fix the request without reading docs.

**Q7: In Kafka, when do you ack and when do you not ack?**  
- **Ack:** On success, or when we decide it’s a business failure (e.g. `BusinessException`) so we don’t retry.  
- **Don’t ack:** On technical/transient failures so that Kafka retries and, after max retries, sends to DLT.

**Q8: What happens if processApprovedApplication throws ResourceNotFoundException at the very beginning?**  
- It propagates to the listener. If the listener doesn’t catch `BusinessException`, the listener won’t ack → retries → DLT. So for consistent behavior, the listener should catch `BusinessException` (and subclasses), log, and ack.

**Q9: Why wrap JsonProcessingException in BadRequestException in User/Offer/Lender services?**  
- So the service API doesn’t declare checked exceptions and the failure is still mapped to 400 with a clear message via our `BadRequestException` (or we could handle `JsonProcessingException` in the global handler; wrapping keeps the contract simple).

**Q10: What is the purpose of the RuntimeException handler in GlobalExceptionHandler?**  
- Fallback for any runtime exception that doesn’t match a more specific handler. Ensures the client always gets a JSON error response instead of an HTML error page.

---

## 9. Quick Reference: Exception → HTTP Status

| Exception | HTTP Status |
|-----------|-------------|
| MethodArgumentNotValidException | 400 |
| ConstraintViolationException | 400 |
| BadRequestException | 400 |
| JsonProcessingException | 400 |
| IllegalArgumentException | 400 |
| HttpMessageNotReadableException | 400 |
| ResourceNotFoundException | 404 |
| NoHandlerFoundException | 404 |
| ConflictException | 409 |
| DataIntegrityViolationException | 409 |
| IllegalStateException | 409 |
| RuntimeException (fallback) | 400 |

(If you add `Exception.class` as catch-all, use 500 and a generic message for that handler.)

---

This should give you a complete picture of your exception handling for discussions or interviews. If you want, we can add a section on “How I would explain this in 2 minutes” or a one-page cheat sheet.
