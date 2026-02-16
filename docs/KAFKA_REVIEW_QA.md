# Kafka in Loan Booking Application – Review Q&A

This document prepares you for project review questions about **Apache Kafka** usage: how you integrated it, how producer/consumer work, and concepts like broker, topic, partition, acknowledgments, and defaults.

---

## 1. Why did you use Kafka in this project?

**Answer:**  
We use Kafka for **asynchronous, decoupled processing** of loan approval. When a user selects an offer and we accept the application (`updateStatus`), we don’t process the full loan creation (Loan + EMI schedules) in the same HTTP request. Instead we:

1. **Update the database** (cancel other applications, set request to IN_PROGRESS) in a transaction.
2. **Publish an event** to Kafka saying “this application is approved for processing.”
3. **Return quickly** to the user (“Your loan application is being processed”).
4. A **Kafka consumer** in the same (or another) service picks up the event and calls `processApprovedApplication`, which creates the Loan and EMIs.

This gives us: **better response time** for the user, **decoupling** between “accept application” and “create loan/EMIs,” and the ability to **scale** or **replay** processing by scaling consumers or reprocessing from the topic.

---

## 2. How is Kafka integrated in your project? (Dependency, configuration, enablement)

**Answer:**  

- **Dependency:** We use **Spring Kafka** via `spring-kafka` in `pom.xml`. It wraps the Kafka client and integrates with Spring Boot.
- **Enablement:** We add **`@EnableKafka`** on the main application class (`LoanBookingApplication.java`) so that `@KafkaListener` methods are discovered and consumer containers are created.
- **Configuration:** In `application.properties` we set:
  - **Broker:** `spring.kafka.bootstrap-servers=localhost:9092`
  - **Producer:** key/value serializers (String and JsonSerializer)
  - **Consumer:** group-id, auto-offset-reset, key/value deserializers (String and JsonDeserializer), and `trusted.packages=*` for JSON
  - **Custom props:** `kafka.topic.loan-approved=loan-approved` and `kafka.group.loan-group=loan-group` for topic and consumer group names.

We rely on **Spring Boot auto-configuration** for `KafkaTemplate` and consumer factory; no custom `@Configuration` bean is required for basic producer/consumer.

---

## 3. Describe the end-to-end flow: from user action to loan creation via Kafka.

**Answer:**  

1. **User selects an offer** → API calls `LoanApplicationService.updateStatus(applicationId)`.
2. **updateStatus (transactional):**  
   - Cancels other pending applications for the same request.  
   - Sets `LoanRequest` status to IN_PROCESS.  
   - Calls **`loanEventProducer.sendLoanApprovedEvent(applicationId)`** → sends a **LoanApprovedEvent** (with `applicationId`) to the Kafka topic **`loan-approved`**.  
   - Returns a response to the user (“loan is being processed”).
3. **Kafka:** The message is stored in the **`loan-approved`** topic. Brokers replicate it according to their configuration.
4. **Consumer:** A **consumer** in our app (same or different instance) is listening on topic `loan-approved` with group `loan-group`. It receives the **LoanApprovedEvent**.
5. **handleLoanApproved:** The listener method calls **`loanApplicationService.processApprovedApplication(event.getApplicationId())`**, which creates the **Loan**, **EMI schedules**, and updates **LoanRequest** and **LoanApplication** status (APPROVED or REJECTED).

So: **REST → Service (DB + produce) → Kafka topic → Consumer → Service (create Loan/EMIs).**

---

## 4. What is a Kafka Producer and how do you use it in this project?

**Answer:**  
A **producer** is a client that **publishes messages** to Kafka topics. In our project we have **`LoanEventProducer`**, which uses Spring’s **`KafkaTemplate<String, Object>`** to send messages.

- We inject **`KafkaTemplate`** (provided by Spring Boot auto-configuration).
- The topic name comes from **`${kafka.topic.loan-approved}`** (e.g. `loan-approved`).
- **`sendLoanApprovedEvent(Long applicationId)`** builds a **`LoanApprovedEvent(applicationId)`** and calls **`kafkaTemplate.send(loanApprovedTopic, event)`**.

We send the **value** as a JSON-serialized object (via `JsonSerializer`); the **key** is not set in our code, so the client uses its default (e.g. round-robin across partitions if no key is provided).

---

## 5. What is a Kafka Consumer and how do you use it?

**Answer:**  
A **consumer** is a client that **reads messages** from Kafka topics. In our project **`LoanApprovedConsumer`** is the consumer.

- We use **`@KafkaListener(topics = "${kafka.topic.loan-approved}", groupId = "${kafka.group.loan-group}")`** on the method **`handleLoanApproved(LoanApprovedEvent event)`**.
- Spring Kafka creates a **consumer** that subscribes to the **`loan-approved`** topic in the **`loan-group`** consumer group.
- When a message arrives, the payload is **deserialized** to **`LoanApprovedEvent`** (using `JsonDeserializer` and trusted packages), and the method is invoked.
- The consumer is **thin**: it only delegates to **`loanApplicationService.processApprovedApplication(event.getApplicationId())`**; all business logic lives in the service.

---

## 6. What is a Broker? What is the broker address in your project?

**Answer:**  
A **broker** is a **Kafka server** that stores topics, receives messages from producers, and serves them to consumers. A Kafka cluster is made of one or more brokers.

In our project we configure a **single broker** (typical for local dev): **`spring.kafka.bootstrap-servers=localhost:9092`**. The producer and consumer use this as the **bootstrap server** to discover the cluster and then talk to the appropriate brokers for each topic/partition.

---

## 7. What is a Topic? Which topic do you use and for what?

**Answer:**  
A **topic** is a **named stream of messages** in Kafka. Producers write to topics; consumers read from topics. Messages in a topic are stored in **partitions** (ordered per partition, not globally).

In our project we use **one topic: `loan-approved`** (name from `kafka.topic.loan-approved`). It carries events indicating that a loan application has been accepted and should be processed (create Loan + EMIs). Each message is a **LoanApprovedEvent** (currently just `applicationId`).

---

## 8. What is a Partition? How do partitions affect your producer?

**Answer:**  
A **partition** is a **ordered, immutable log** inside a topic. A topic has one or more partitions. Messages with the same **key** go to the same partition (when a key is provided); ordering is guaranteed only **within** a partition.

In our code we call **`kafkaTemplate.send(loanApprovedTopic, event)`** without a **key**. So the producer uses the default behavior: without a key, the client typically **distributes messages in round-robin** across partitions (or uses a sticky partitioner). We don’t rely on ordering per application in the consumer; if we needed ordering per loan request, we could send the key as e.g. `loanRequestId` so all events for that request go to the same partition.

---

## 9. What is a Consumer Group? What group do you use and why?

**Answer:**  
A **consumer group** is a set of consumers that **share the consumption** of one or more topics. Each partition of a topic is consumed by **at most one** consumer in the group. So you can scale by adding more consumers in the same group (up to the number of partitions).

We use the group **`loan-group`** (`kafka.group.loan-group`). All our application instances that run **`LoanApprovedConsumer`** with this group share the load: each partition of `loan-approved` is read by one consumer instance. This gives us **load balancing** and **no duplicate processing** of the same message within the group (assuming normal commit behavior).

---

## 10. What are acknowledgments (ACKs) in Kafka? What does your project use by default?

**Answer:**  
**Acknowledgments** refer to when the broker considers a produced message “committed” and when the consumer is considered to have “processed” a message.

- **Producer ACKs:**  
  The producer can wait for different levels of confirmation from the broker:
  - **acks=0:** no wait (fire-and-forget).  
  - **acks=1:** leader replica has written (default in many setups).  
  - **acks=all (-1):** leader + all in-sync replicas have written (strongest durability).

  We don’t override this in our project, so we use **Spring Boot’s default** for the producer (often **acks=1** or whatever the Kafka client default is). For critical loan events, in production you might set **acks=all** and consider **retries** and **idempotence**.

- **Consumer commit (offset):**  
  The consumer “acknowledges” by **committing its offset** (position in the partition). Spring Kafka by default uses **commit after the listener method returns successfully** (e.g. **enable.auto.commit=false** with **manual commit** after processing, or equivalent “commit on success”). If the listener throws, the offset is not committed, so the message can be **redelivered** (at-least-once behavior).

---

## 11. What is the default nature of producer send (sync vs async)? Do you wait for the result?

**Answer:**  
**`KafkaTemplate.send(...)`** is **asynchronous** by default: it returns a **`CompletableFuture`** (or similar) and does not block. We don’t call **`.get()`** or **`.join()`** in our code, so we **fire-and-forget**: we don’t wait for the broker to acknowledge the message or for the send to complete.

**Implications:**  
- The HTTP response can return before the message is actually written to Kafka.  
- If the send fails, we don’t know in the API and don’t retry from the service (we could add callback or block for critical flows).  
- For review: you can say we use **async send** for responsiveness and that for production you might add **callbacks** or **sync get** and error handling for critical events.

---

## 12. What serialization do you use for producer and consumer? Why JSON?

**Answer:**  

- **Producer:**  
  - Key: **`StringSerializer`** (we don’t set a key in code; if we did, it would be a string).  
  - Value: **`JsonSerializer`** – the **LoanApprovedEvent** object is serialized to JSON.

- **Consumer:**  
  - Key: **`StringDeserializer`**.  
  - Value: **`JsonDeserializer`** – JSON is deserialized to **LoanApprovedEvent**.  
  - We set **`spring.kafka.consumer.properties.spring.json.trusted.packages=*`** so that the deserializer can instantiate our event class from the package of our application (security note: in production you might restrict this to specific packages).

We use **JSON** so that the event payload is **human-readable**, **easy to evolve** (add fields), and **language-agnostic** if we add non-Java consumers later.

---

## 13. What is auto-offset-reset and what value do you use?

**Answer:**  
**auto-offset-reset** tells Kafka what to do when the consumer group has **no committed offset** for a partition (e.g. first time the group runs, or offset was invalid/lost).

- **earliest:** Start from the **beginning** of the partition (replay all messages).  
- **latest:** Start from the **end** (only new messages).  
- **none:** Throw an exception if no offset is found.

We use **`spring.kafka.consumer.auto-offset-reset=earliest`**. So when our consumer group starts for the first time (or has no valid offset), it reads from the beginning of the topic and won’t miss earlier “loan-approved” events.

---

## 14. What happens if the consumer fails while processing a message (e.g. DB error)?

**Answer:**  
In our consumer we call **`loanApplicationService.processApprovedApplication(event.getApplicationId())`**. If that method **throws** (e.g. DB exception, validation failure), we **log and rethrow** (`throw e`). When the listener throws, Spring Kafka typically **does not commit** the offset for that message. So the same message will be **redelivered** after a rebalance or on the next poll (at-least-once semantics).

**Implications:**  
- We might process the same application **more than once** if the first attempt failed after doing some work. Our **processApprovedApplication** is written to be **idempotent-friendly**: we load the application and either create Loan+EMIs once or set REJECTED; duplicate processing could lead to duplicate Loan/EMI creation if we don’t add extra guards (e.g. “if Loan already exists for this application, skip”).  
- For review: we get **at-least-once** delivery; for exactly-once we’d need idempotent consumer logic and/or transactional/ exactly-once Kafka features.

---

## 15. How does Kafka relate to transactions in your project?

**Answer:**  
Kafka is **outside** the database transaction:

- In **updateStatus** we run inside a **single DB transaction**: cancel others, set IN_PROCESS, save. Then we **send the Kafka event after** the transaction (still in the same method). So either all DB changes commit and then we produce, or we roll back and never produce (we don’t produce inside the transactional block in a way that would commit before DB commit).
- The **consumer** runs in a **separate thread/process**. When it calls **processApprovedApplication**, that method is **@Transactional**: it creates Loan and EMIs and updates status in one DB transaction. So: **Kafka is used for triggering work**; the **consistency of the database** is handled by Spring transactions in the service, not by Kafka.

We do **not** use Kafka transactions (e.g. “read-process-write” with transactional producer and consumer) in this project.

---

## 16. Summary table: Kafka components in this project

| Component        | In this project                                                                 |
|-----------------|-----------------------------------------------------------------------------------|
| **Broker**      | `localhost:9092` (single broker, dev)                                            |
| **Topic**       | `loan-approved`                                                                  |
| **Producer**    | `LoanEventProducer` using `KafkaTemplate.send(topic, LoanApprovedEvent)`         |
| **Consumer**    | `LoanApprovedConsumer` with `@KafkaListener` on `loan-approved`, group `loan-group` |
| **Message key** | Not set (optional; could use e.g. loanRequestId for ordering)                    |
| **Message value** | `LoanApprovedEvent` (JSON) with `applicationId`                               |
| **Producer serialization** | Key: String, Value: JsonSerializer                                      |
| **Consumer deserialization** | Key: String, Value: JsonDeserializer → LoanApprovedEvent              |
| **Consumer group** | `loan-group`                                                                  |
| **Auto offset reset** | `earliest`                                                                  |
| **Send**        | Asynchronous (no blocking on send result)                                        |
| **Delivery**    | At-least-once (offset committed on success; throw on failure to retry)          |

---

Use this document to explain how Kafka is used in your Loan Booking Application and to answer follow-up questions on producers, consumers, brokers, topics, partitions, ACKs, and default behavior in your project.
