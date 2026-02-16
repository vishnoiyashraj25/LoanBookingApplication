# Indexing Guide for Loan Booking Application

This document explains **what indexing is**, **what types you can use** in your JPA entities, and **concrete recommendations** per entity based on your current queries and relationships.

---

## 1. Why Indexing Matters

- **Without indexes**: The database scans entire tables (full table scan) for `WHERE`, `JOIN`, and `ORDER BY` → slow as data grows.
- **With indexes**: The database uses a structure (e.g. B-tree) to find rows quickly by key columns → much faster lookups and joins.

Trade-off: indexes speed up **reads** but add some cost to **writes** (insert/update/delete) and use extra storage. For a loan application (read-heavy for listings, dashboards, reports), adding the right indexes is usually beneficial.

---

## 2. Types of Indexes You Can Use

### 2.1 In JPA / Hibernate (declarative)

| Type | How you define it | Use when |
|------|-------------------|----------|
| **Single-column index** | `@Table(indexes = @Index(name = "idx_col", columnList = "column_name"))` | One column used in WHERE / JOIN / ORDER BY |
| **Composite index** | `@Index(columnList = "col1, col2")` | Queries filter/sort by multiple columns together |
| **Unique constraint** | `@Table(uniqueConstraints = ...)` | Uniqueness (e.g. email, PAN); also creates a unique index |
| **FK columns** | Often **not** auto-indexed by JPA; add explicitly with `@Index` on the join column | Every `@JoinColumn` used in JOINs or “find by relation” |

### 2.2 What your DB already gives you

- **Primary key**: Every table has an index on the PK (e.g. `id`, `loan_number`).
- **Unique columns**: `unique = true` on `@Column` typically creates a unique index (e.g. `User.email`, `Account.accountNumber`).

So the main things to add explicitly are:

- Indexes on **foreign key columns** (e.g. `offer_id`, `loan_request_id`, `user_id`, `loan_number`).
- Indexes for **frequently filtered/sorted columns** (e.g. `status`, `dueDate`, `receivedAt`).
- **Composite indexes** for queries that filter by multiple columns (e.g. `(loan_request_id, status)`).

---

## 3. Entity-by-Entity Indexing

Below, “query pattern” is derived from your repositories and services.

---

### 3.1 User

**Existing:** PK on `userId`, unique indexes on `email`, `phoneNumber`, `aadharNumber`, `panNumber` (from `unique = true`).

**Suggested extra indexes:**

| Index | Column(s) | Reason |
|-------|-----------|--------|
| `idx_users_employee_type` | `employee_type` | `findByEmployeeType` |
| `idx_users_kyc_verified` | `kyc_verified` | `findByKycVerified` |
| `idx_users_employee_kyc` | `employee_type, kyc_verified` | `findByEmployeeTypeAndKycVerified` (composite) |

Optional: `idx_users_user_id_employee_type` and `idx_users_user_id_kyc` if you use `findByUserIdAndEmployeeType` / `findByUserIdAndKycVerified` often; otherwise the PK + one of the above may be enough.

---

### 3.2 Account

**Existing:** PK on `id`, unique on `accountNumber`.  
**Join:** `user_id` (FK to User).

**Suggested:**

| Index | Column(s) | Reason |
|-------|-----------|--------|
| `idx_accounts_user_id` | `user_id` | `findByUser(User)` (FK lookup) |
| `idx_accounts_type` | `type` | `findByType(AccountType)` |
| `idx_accounts_id_type` | `id, type` | `findByIdAndType` (composite) |

---

### 3.3 Lender

**Existing:** PK on `lenderId`, unique on `lenderName`.

**Suggested:**

| Index | Column(s) | Reason |
|-------|-----------|--------|
| `idx_lenders_lender_type` | `lender_type` | `findByLenderType` |

`lenderName` is already unique (indexed). No FK here.

---

### 3.4 Offer

**Existing:** PK on `offerId`, and a **unique constraint** on `(lender_id, loan_type, min_tenure, max_tenure, interest_rate, max_amount)` (which creates a unique index).  
**Join:** `lender_id`.

**Suggested:**

| Index | Column(s) | Reason |
|-------|-----------|--------|
| `idx_offers_lender_id` | `lender_id` | `findByLender_*`, JOINs from LoanApplication |
| `idx_offers_loan_type` | `loan_type` | `findByLoanType` |
| `idx_offers_status` | `status` | `findByStatus` |
| `idx_offers_lender_status` | `lender_id, status` | Queries by lender + active/inactive offers (composite) |

The unique constraint already helps for lookups on that combination; the above support other query patterns.

---

### 3.5 LoanRequest

**Joins:** `user_id`.

**Suggested:**

| Index | Column(s) | Reason |
|-------|-----------|--------|
| `idx_loan_request_user_id` | `user_id` | `findByUser(User)` (FK lookup) |
| `idx_loan_request_status` | `request_status` | Filter by status (e.g. PENDING, APPROVED) |

---

### 3.6 LoanApplication

**Joins:** `offer_id`, `loan_request_id`.  
**Query patterns:** `findByStatus`, `findByOffer_Lender_LenderName`, `findByLoanRequest_User_PanNumber`, `findByLoanRequest`, `findByLoanRequestAndStatus`.

**Suggested:**

| Index | Column(s) | Reason |
|-------|-----------|--------|
| `idx_loan_applications_offer_id` | `offer_id` | JOIN to Offer, lookups by offer |
| `idx_loan_applications_loan_request_id` | `loan_request_id` | `findByLoanRequest`, JOIN to LoanRequest |
| `idx_loan_applications_status` | `status` | `findByStatus` |
| `idx_loan_applications_request_status` | `loan_request_id, status` | `findByLoanRequestAndStatus` (composite) |
| `idx_loan_applications_created_at` | `created_at` | Reports, “recent applications” (optional) |

For `findByLoanRequest_User_PanNumber` and `findByOffer_Lender_LenderName`, the DB will use the FK indexes on `loan_request_id` and `offer_id` and then join to User/Lender; that’s usually sufficient. If you have very heavy querying by `panNumber` or `lenderName` on this table, you could consider indexes on the joined tables (User, Lender) as above; no need to duplicate columns in `loan_applications` for that.

---

### 3.7 Loan

**PK:** `loan_number`.  
**Joins:** `lender_id`, `user_id`, `loan_application_id`, `disbursement_acc_id`.

**Suggested:**

| Index | Column(s) | Reason |
|-------|-----------|--------|
| `idx_loans_lender_id` | `lender_id` | JOINs, “loans by lender” |
| `idx_loans_user_id` | `user_id` | “My loans” by user |
| `idx_loans_loan_application_id` | `loan_application_id` | One-to-one link to LoanApplication |
| `idx_loans_status` | `status` | Filter by ACTIVE, CLOSED, etc. |
| `idx_loans_disbursement_acc_id` | `disbursement_acc_id` | JOIN to Account (nullable FK) |

---

### 3.8 EmiSchedule

**Join:** `loan_number` (FK to Loan).  
**Query:** `findFirstByLoanAndStatusOrderByDueDateAsc(loan, status)`.

**Suggested:**

| Index | Column(s) | Reason |
|-------|-----------|--------|
| `idx_emi_schedule_loan_number` | `loan_number` | JOIN to Loan, filter by loan |
| `idx_emi_schedule_loan_status_due` | `loan_number, status, due_date` | Composite for “first EMI by loan and status, ordered by due_date” |

---

### 3.9 Payment

**Joins:** `loan_number`, `emi_id`.  
**Typical use:** List/filter payments by loan or by EMI, and by time.

**Suggested:**

| Index | Column(s) | Reason |
|-------|-----------|--------|
| `idx_payments_loan_number` | `loan_number` | “Payments for this loan” |
| `idx_payments_emi_id` | `emi_id` | “Payments for this EMI” |
| `idx_payments_received_at` | `received_at` | Time-range queries, reporting (optional) |
| `idx_payments_transaction_id` | `transaction_id` | Idempotency / duplicate check (optional; add as unique if one payment per transaction) |

---

## 4. Summary Table

| Entity         | Suggested indexes (besides PK / existing unique) |
|----------------|--------------------------------------------------|
| **User**       | `employee_type`, `kyc_verified`, `(employee_type, kyc_verified)` |
| **Account**    | `user_id`, `type`, `(id, type)` |
| **Lender**     | `lender_type` |
| **Offer**      | `lender_id`, `loan_type`, `status`, optional `(lender_id, status)` |
| **LoanRequest**| `user_id`, `request_status` |
| **LoanApplication** | `offer_id`, `loan_request_id`, `status`, `(loan_request_id, status)`, optional `created_at` |
| **Loan**       | `lender_id`, `user_id`, `loan_application_id`, `status`, `disbursement_acc_id` |
| **EmiSchedule**| `loan_number`, `(loan_number, status, due_date)` |
| **Payment**    | `loan_number`, `emi_id`, optional `received_at`, optional `transaction_id` |

---

## 5. How to Add Indexes in Your Code (JPA)

Example for **LoanApplication** (single-column and composite):

```java
@Entity
@Table(name = "loan_applications", indexes = {
    @Index(name = "idx_loan_applications_offer_id", columnList = "offer_id"),
    @Index(name = "idx_loan_applications_loan_request_id", columnList = "loan_request_id"),
    @Index(name = "idx_loan_applications_status", columnList = "status"),
    @Index(name = "idx_loan_applications_request_status", columnList = "loan_request_id, status"),
    @Index(name = "idx_loan_applications_created_at", columnList = "created_at")
})
public class LoanApplication {
    // ...
}
```

Rules:

- `columnList`: use **database column names** (e.g. `offer_id`, not `offer`). Same as in `@JoinColumn(name = "offer_id")`.
- Order in a composite index matters: put equality filters first, then range/sort (e.g. `loan_request_id, status` then `due_date` in EMIs).
- With `spring.jpa.hibernate.ddl-auto=update` or `create`, Hibernate will create these indexes on next startup (for production, prefer managed migrations e.g. Flyway/Liquibase).

---

## 6. Things to Watch

- **Don’t over-index**: Too many indexes slow down inserts/updates and take space. Add indexes that match real query patterns.
- **Measure**: Use your DB’s `EXPLAIN` / “explain plan” on important queries to confirm they use the new indexes.
- **Migrations**: For production, create indexes via migration scripts so you control when they’re added and can do it in maintenance windows if needed (some DBs allow `CREATE INDEX CONCURRENTLY` to reduce locking).

If you want, the next step can be applying these indexes directly in your entity classes (with concrete `@Table(indexes = { ... })` and, where useful, `uniqueConstraints`) so they’re generated with your current DDL strategy.
